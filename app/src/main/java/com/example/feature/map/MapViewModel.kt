package com.example.feature.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.config.PilotCityConfig
import com.example.core.location.DeviceLocationResolver
import com.example.core.location.DeviceLocationResult
import com.example.core.model.GeoBoundingBox
import com.example.core.model.GeoCoordinate
import com.example.core.model.IncidentStatus
import com.example.data.incidents.IncidentDocument
import com.example.data.incidents.IncidentRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.abs

data class MapFilterState(
    val bounds: GeoBoundingBox = PilotCityConfig.salvador.initialBounds,
    val zoomLevel: Double = PilotCityConfig.salvador.defaultZoom.toDouble(),
    val categorySlug: String? = null,
    val statusFilter: IncidentStatus? = null,
)

sealed interface MapContentState {
    data object Loading : MapContentState
    data class Ready(
        val incidents: List<IncidentDocument>,
        val renderNodes: List<MapRenderNode>,
    ) : MapContentState
    data class Error(val message: String) : MapContentState
}

data class MapUiMessage(
    val text: String,
    val isError: Boolean = false,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val incidentRepository: IncidentRepository,
) : ViewModel() {

    private val _filterState = MutableStateFlow(MapFilterState())
    val filterState: StateFlow<MapFilterState> = _filterState.asStateFlow()

    private val _selectedIncident = MutableStateFlow<IncidentDocument?>(null)
    val selectedIncident: StateFlow<IncidentDocument?> = _selectedIncident.asStateFlow()

    private val _isBusyLocationOrSearch = MutableStateFlow(false)
    val isBusyLocationOrSearch: StateFlow<Boolean> = _isBusyLocationOrSearch.asStateFlow()

    private val _statusBanner = MutableStateFlow<MapUiMessage?>(null)
    val statusBanner: StateFlow<MapUiMessage?> = _statusBanner.asStateFlow()

    private val _cameraTarget = MutableStateFlow<Pair<GeoCoordinate, Double>?>(null)
    val cameraTarget: StateFlow<Pair<GeoCoordinate, Double>?> = _cameraTarget.asStateFlow()

    var lastMapCenter: GeoCoordinate = PilotCityConfig.salvador.center
        private set
    var lastMapZoom: Double = PilotCityConfig.salvador.defaultZoom.toDouble()
        private set

    private val _currentLocalityTitle = MutableStateFlow("Salvador, BA")
    val currentLocalityTitle: StateFlow<String> = _currentLocalityTitle.asStateFlow()

    private var lastGeocodedCenter: GeoCoordinate? = null

    val mapContentState: StateFlow<MapContentState> = _filterState
        .map { state -> Triple(state.bounds, state.categorySlug, state.statusFilter) }
        .distinctUntilChanged()
        .debounce(250L)
        .flatMapLatest { (bounds, categorySlug, statusFilter) ->
            incidentRepository.observeIncidentsInBounds(
                bounds = bounds,
                categorySlug = categorySlug,
                statusFilter = statusFilter,
            ).combine(_filterState.map { it.zoomLevel }.distinctUntilChanged()) { incidents, zoom ->
                MapContentState.Ready(
                    incidents = incidents,
                    renderNodes = IncidentClusterer.cluster(incidents, zoom),
                ) as MapContentState
            }.catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Erro ao consultar área visível no mapa", rootError)
                emit(
                    MapContentState.Error(
                        rootError.message ?: "Não foi possível carregar ocorrências nesta área.",
                    ),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MapContentState.Loading,
        )

    val localAreaInsight: StateFlow<LocalAreaInsight> = combine(
        mapContentState,
        _currentLocalityTitle,
    ) { content, localityTitle ->
        val incidents = (content as? MapContentState.Ready)?.incidents.orEmpty()
        LocalAreaInsightCalculator.compute(
            localityTitle = localityTitle,
            incidents = incidents,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = LocalAreaInsightCalculator.compute(
            localityTitle = "Salvador, BA",
            incidents = emptyList(),
        ),
    )

    fun updateViewport(bounds: GeoBoundingBox, zoomLevel: Double) {
        lastMapCenter = GeoCoordinate(
            latitude = (bounds.north + bounds.south) / 2.0,
            longitude = (bounds.east + bounds.west) / 2.0,
        )
        lastMapZoom = zoomLevel
        _filterState.update { current ->
            current.copy(bounds = bounds, zoomLevel = zoomLevel)
        }
    }

    fun selectCategoryFilter(slug: String?) {
        _filterState.update { current ->
            val next = if (current.categorySlug == slug) null else slug
            current.copy(categorySlug = next)
        }
    }

    fun selectStatusFilter(status: IncidentStatus?) {
        _filterState.update { current ->
            val next = if (current.statusFilter == status) null else status
            current.copy(statusFilter = next)
        }
    }

    fun clearAllFilters() {
        _filterState.update { it.copy(categorySlug = null, statusFilter = null) }
    }

    fun selectIncident(incident: IncidentDocument?) {
        _selectedIncident.value = incident
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun consumeCameraTarget() {
        _cameraTarget.value = null
    }

    fun recenterOnSalvador() {
        _statusBanner.value = null
        _currentLocalityTitle.value = "Centro · Salvador, BA"
        val target = PilotCityConfig.salvador.center
        val zoom = PilotCityConfig.salvador.defaultZoom.toDouble()
        lastMapCenter = target
        lastMapZoom = zoom
        _cameraTarget.value = target to zoom
    }

    fun focusNeighborhood(name: String, coordinate: GeoCoordinate, zoom: Double = 15.0) {
        _statusBanner.value = null
        _currentLocalityTitle.value = "$name · Salvador"
        lastGeocodedCenter = coordinate
        lastMapCenter = coordinate
        lastMapZoom = zoom
        _cameraTarget.value = coordinate to zoom
    }

    fun focusCoordinate(
        coordinate: GeoCoordinate,
        localityLabel: String? = null,
        zoom: Double = 15.5,
    ) {
        _statusBanner.value = null
        if (!localityLabel.isNullOrBlank()) {
            _currentLocalityTitle.value = localityLabel
        }
        lastGeocodedCenter = coordinate
        lastMapCenter = coordinate
        lastMapZoom = zoom
        val deltaLat = 0.04
        val deltaLng = 0.04
        _filterState.update { current ->
            current.copy(
                bounds = GeoBoundingBox(
                    north = (coordinate.latitude + deltaLat).coerceAtMost(90.0),
                    south = (coordinate.latitude - deltaLat).coerceAtLeast(-90.0),
                    east = (coordinate.longitude + deltaLng).coerceAtMost(180.0),
                    west = (coordinate.longitude - deltaLng).coerceAtLeast(-180.0),
                ),
                zoomLevel = zoom,
            )
        }
        _cameraTarget.value = coordinate to zoom
    }

    fun refreshLocalityForBounds(context: Context, bounds: GeoBoundingBox) {
        val centerLat = (bounds.north + bounds.south) / 2.0
        val centerLng = (bounds.east + bounds.west) / 2.0
        val previous = lastGeocodedCenter
        if (previous != null &&
            abs(previous.latitude - centerLat) < 0.007 &&
            abs(previous.longitude - centerLng) < 0.007
        ) {
            return
        }
        lastGeocodedCenter = GeoCoordinate(centerLat, centerLng)
        viewModelScope.launch {
            val resolved = reverseGeocodeLocality(context, centerLat, centerLng)
            if (!resolved.isNullOrBlank()) {
                _currentLocalityTitle.value = resolved
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun moveToCurrentDeviceLocation(context: Context) {
        viewModelScope.launch {
            _isBusyLocationOrSearch.value = true
            _statusBanner.value = null
            try {
                when (val res = DeviceLocationResolver.getCurrentCoordinate(context)) {
                    is DeviceLocationResult.Success -> {
                        val target = res.coordinate
                        lastMapCenter = target
                        lastMapZoom = 16.0
                        _cameraTarget.value = target to 16.0
                        val resolved = reverseGeocodeLocality(context, target.latitude, target.longitude)
                        if (!resolved.isNullOrBlank()) {
                            _currentLocalityTitle.value = resolved
                        }
                    }
                    is DeviceLocationResult.Failure -> {
                        _statusBanner.value = MapUiMessage(
                            text = res.message,
                            isError = true,
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Falha ao obter GPS", e)
                _statusBanner.value = MapUiMessage(
                    text = "Erro ao acessar GPS: ${e.localizedMessage ?: "verifique as permissões."}",
                    isError = true,
                )
            } finally {
                _isBusyLocationOrSearch.value = false
            }
        }
    }

    fun searchLocationInSalvador(context: Context, rawQuery: String) {
        val query = rawQuery.trim()
        if (query.length < 2) {
            _statusBanner.value = MapUiMessage(
                text = "Digite pelo menos 2 caracteres para buscar um bairro ou via.",
                isError = true,
            )
            return
        }
        viewModelScope.launch {
            _isBusyLocationOrSearch.value = true
            _statusBanner.value = null
            try {
                val scopedQuery = if (query.contains("salvador", ignoreCase = true)) {
                    query
                } else {
                    "$query, Salvador, Bahia, Brasil"
                }
                val address = geocodeAddress(context, scopedQuery)
                if (address != null) {
                    val target = GeoCoordinate(address.latitude, address.longitude)
                    lastMapCenter = target
                    lastMapZoom = 15.5
                    _cameraTarget.value = target to 15.5
                    val label = address.subLocality ?: address.thoroughfare ?: address.featureName ?: query
                    _currentLocalityTitle.value = "$label · Salvador, BA"
                    lastGeocodedCenter = GeoCoordinate(address.latitude, address.longitude)
                    _statusBanner.value = MapUiMessage("Mapa centralizado em: $label")
                } else {
                    _statusBanner.value = MapUiMessage(
                        text = "Nenhum local encontrado para \"$query\" em Salvador.",
                        isError = true,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Falha no Geocoder", e)
                _statusBanner.value = MapUiMessage(
                    text = "Busca de endereço indisponível no momento sem conexão.",
                    isError = true,
                )
            } finally {
                _isBusyLocationOrSearch.value = false
            }
        }
    }

    private suspend fun geocodeAddress(context: Context, query: String): Address? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale("pt", "BR"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocationName(
                        query,
                        1,
                        PilotCityConfig.salvador.initialBounds.south,
                        PilotCityConfig.salvador.initialBounds.west,
                        PilotCityConfig.salvador.initialBounds.north,
                        PilotCityConfig.salvador.initialBounds.east,
                    ) { addresses ->
                        if (cont.isActive) cont.resume(addresses.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(
                    query,
                    1,
                    PilotCityConfig.salvador.initialBounds.south,
                    PilotCityConfig.salvador.initialBounds.west,
                    PilotCityConfig.salvador.initialBounds.north,
                    PilotCityConfig.salvador.initialBounds.east,
                )?.firstOrNull()
            }
        }

    private suspend fun reverseGeocodeLocality(
        context: Context,
        latitude: Double,
        longitude: Double,
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale("pt", "BR"))
            val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (cont.isActive) cont.resume(addresses.firstOrNull())
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            } ?: return@withContext null

            val subLocality = address.subLocality?.trim()?.takeIf { it.isNotBlank() }
            val thoroughfare = address.thoroughfare?.trim()?.takeIf { it.isNotBlank() }
            val city = address.locality ?: address.subAdminArea ?: "Salvador"
            val neighborhood = subLocality ?: thoroughfare
            if (!neighborhood.isNullOrBlank() && !neighborhood.equals(city, ignoreCase = true)) {
                "$neighborhood · $city"
            } else {
                "$city, BA"
            }
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val TAG = "MapVM"
    }
}
