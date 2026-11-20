package com.example.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.example.core.model.GeoCoordinate
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed interface DeviceLocationResult {
    data class Success(val coordinate: GeoCoordinate) : DeviceLocationResult
    data class Failure(
        val message: String,
        val locationDisabled: Boolean = false,
        val permissionMissing: Boolean = false,
    ) : DeviceLocationResult
}

/**
 * Resolve a localização atual real do aparelho usando múltiplas camadas do Android:
 * 1) FusedLocationProviderClient (lastLocation recente -> HIGH_ACCURACY -> BALANCED_POWER_ACCURACY)
 * 2) Fallback nativo via LocationManagerCompat (GPS_PROVIDER / NETWORK_PROVIDER)
 * 3) Melhor última posição conhecida válida entre os provedores do aparelho.
 */
object DeviceLocationResolver {

    private const val RECENT_FIX_MAX_AGE_MS = 5 * 60 * 1000L

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinate(context: Context): DeviceLocationResult {
        val appContext = context.applicationContext
        if (!hasLocationPermission(appContext)) {
            return DeviceLocationResult.Failure(
                message = "Permissão de localização não concedida. Use 'Endereço' ou 'Centro SSA'.",
                permissionMissing = true,
            )
        }

        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isLocationEnabled = locationManager?.let { LocationManagerCompat.isLocationEnabled(it) } ?: true
        if (!isLocationEnabled) {
            return DeviceLocationResult.Failure(
                message = "O GPS / Localização do aparelho está desligado. Ative nas Configurações ou use 'Endereço' / 'Centro SSA'.",
                locationDisabled = true,
            )
        }

        // 1. Tenta último fix recente ou leitura ativa pelo FusedLocationProviderClient
        val fusedLast = tryGetFusedLastLocation(appContext)
        if (fusedLast != null && isRecentAndValid(fusedLast)) {
            return DeviceLocationResult.Success(GeoCoordinate(fusedLast.latitude, fusedLast.longitude))
        }

        val fusedHigh = tryGetFusedCurrentLocation(appContext, Priority.PRIORITY_HIGH_ACCURACY, 6_500L)
        if (fusedHigh != null && isValidCoordinate(fusedHigh)) {
            return DeviceLocationResult.Success(GeoCoordinate(fusedHigh.latitude, fusedHigh.longitude))
        }

        val fusedBalanced = tryGetFusedCurrentLocation(appContext, Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3_500L)
        if (fusedBalanced != null && isValidCoordinate(fusedBalanced)) {
            return DeviceLocationResult.Success(GeoCoordinate(fusedBalanced.latitude, fusedBalanced.longitude))
        }

        // 2. Fallback para provedores nativos do Android (útil quando Play Services está frio ou em emuladores)
        if (locationManager != null) {
            val providersToTry = buildList {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    add(LocationManager.GPS_PROVIDER)
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    add(LocationManager.NETWORK_PROVIDER)
                }
            }
            for (provider in providersToTry) {
                val nativeFix = tryGetNativeCurrentLocation(appContext, locationManager, provider, 4_500L)
                if (nativeFix != null && isValidCoordinate(nativeFix)) {
                    return DeviceLocationResult.Success(GeoCoordinate(nativeFix.latitude, nativeFix.longitude))
                }
            }
        }

        // 3. Última posição conhecida armazenada em qualquer provedor disponível
        val bestCached = listOfNotNull(
            fusedLast,
            getBestLastKnownLocation(locationManager),
        )
            .filter(::isValidCoordinate)
            .maxByOrNull { it.time }

        if (bestCached != null) {
            return DeviceLocationResult.Success(GeoCoordinate(bestCached.latitude, bestCached.longitude))
        }

        return DeviceLocationResult.Failure(
            message = "Sinal de GPS indisponível neste momento. Verifique se a localização do aparelho está ativa ou toque em 'Endereço' / 'Centro SSA'.",
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetFusedLastLocation(context: Context): Location? {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            withTimeoutOrNull(2_000L) {
                client.lastLocation.await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetFusedCurrentLocation(
        context: Context,
        priority: Int,
        timeoutMs: Long,
    ): Location? {
        val tokenSource = CancellationTokenSource()
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            withTimeoutOrNull(timeoutMs) {
                client.getCurrentLocation(priority, tokenSource.token).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        } finally {
            tokenSource.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun tryGetNativeCurrentLocation(
        context: Context,
        locationManager: LocationManager,
        provider: String,
        timeoutMs: Long,
    ): Location? {
        val signal = CancellationSignal()
        return try {
            withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    cont.invokeOnCancellation { signal.cancel() }
                    try {
                        LocationManagerCompat.getCurrentLocation(
                            locationManager,
                            provider,
                            signal,
                            ContextCompat.getMainExecutor(context),
                        ) { location ->
                            if (cont.isActive) {
                                cont.resume(location)
                            }
                        }
                    } catch (_: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        } finally {
            signal.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(locationManager: LocationManager?): Location? {
        if (locationManager == null) return null
        val providers = runCatching { locationManager.getProviders(true) }.getOrNull().orEmpty()
        return providers.mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
            .filter(::isValidCoordinate)
            .maxByOrNull { it.time }
    }

    private fun isValidCoordinate(location: Location): Boolean {
        return !(location.latitude == 0.0 && location.longitude == 0.0) &&
            location.latitude in -90.0..90.0 &&
            location.longitude in -180.0..180.0
    }

    private fun isRecentAndValid(location: Location): Boolean {
        if (!isValidCoordinate(location)) return false
        val ageMs = System.currentTimeMillis() - location.time
        return ageMs in 0L..RECENT_FIX_MAX_AGE_MS
    }
}
