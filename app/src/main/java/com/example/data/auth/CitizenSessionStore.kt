package com.example.data.auth

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class LocalCitizenSession(
    val guestId: String,
    val displayName: String,
    val homeCityCode: String = "ssa-ba",
)

/**
 * Armazena no aparelho um identificador local não sensível (`guest_<uuid>`) e nome de exibição
 * opcional para permitir envio de ocorrências e confirmações comunitárias sem exigir login Google.
 */
class CitizenSessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _session = MutableStateFlow(loadOrCreateSession())
    val session: StateFlow<LocalCitizenSession> = _session.asStateFlow()

    fun currentGuestId(): String = _session.value.guestId

    fun updateDisplayName(newName: String) {
        val cleaned = newName.trim().ifBlank { DEFAULT_DISPLAY_NAME }.take(80)
        prefs.edit().putString(KEY_DISPLAY_NAME, cleaned).apply()
        _session.value = _session.value.copy(displayName = cleaned)
    }

    fun resetLocalIdentity() {
        val freshId = generateGuestId()
        prefs.edit()
            .putString(KEY_GUEST_ID, freshId)
            .putString(KEY_DISPLAY_NAME, DEFAULT_DISPLAY_NAME)
            .apply()
        _session.value = LocalCitizenSession(
            guestId = freshId,
            displayName = DEFAULT_DISPLAY_NAME,
        )
    }

    private fun loadOrCreateSession(): LocalCitizenSession {
        val existingId = prefs.getString(KEY_GUEST_ID, null)
            ?.takeIf { isValidGuestId(it) }
        val guestId = existingId ?: generateGuestId().also { created ->
            prefs.edit().putString(KEY_GUEST_ID, created).apply()
        }
        val displayName = prefs.getString(KEY_DISPLAY_NAME, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_DISPLAY_NAME
        return LocalCitizenSession(
            guestId = guestId,
            displayName = displayName,
        )
    }

    companion object {
        private const val PREFS_NAME = "urban_alert_citizen_session"
        private const val KEY_GUEST_ID = "guest_id"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val DEFAULT_DISPLAY_NAME = "Cidadão de Salvador"
        private val GUEST_ID_REGEX = Regex("^guest_[a-zA-Z0-9_\\-]+$")

        fun generateGuestId(): String = "guest_${UUID.randomUUID()}"

        fun isValidGuestId(id: String): Boolean =
            id.length in 10..64 && GUEST_ID_REGEX.matches(id)
    }
}
