package cl.agnov.ameli.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.agnov.ameli.sip.model.AudioCodec
import cl.agnov.ameli.sip.model.SipAccountPreferences
import cl.agnov.ameli.sip.model.SipTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ameli_preferences")

/** Permite sustituir [PreferencesRepository] por un fake en pruebas unitarias. */
interface AccountPreferencesStore {
    val accountPreferences: Flow<SipAccountPreferences?>
    suspend fun saveAccountPreferences(preferences: SipAccountPreferences)
    suspend fun clearAccountPreferences()
}

/**
 * Persiste la configuración de la cuenta SIP que no es sensible (todo salvo
 * las contraseñas, que viven en [SecureCredentialStore]).
 */
class PreferencesRepository(private val context: Context) : AccountPreferencesStore {

    private object Keys {
        val USERNAME = stringPreferencesKey("sip_username")
        val DOMAIN = stringPreferencesKey("sip_domain")
        val PORT = intPreferencesKey("sip_port")
        val TRANSPORT = stringPreferencesKey("sip_transport")
        val DISPLAY_NAME = stringPreferencesKey("sip_display_name")
        val SRTP_ENABLED = booleanPreferencesKey("sip_srtp_enabled")
        val STUN_ENABLED = booleanPreferencesKey("sip_stun_enabled")
        val STUN_SERVER = stringPreferencesKey("sip_stun_server")
        val ICE_ENABLED = booleanPreferencesKey("sip_ice_enabled")
        val TURN_ENABLED = booleanPreferencesKey("sip_turn_enabled")
        val TURN_SERVER = stringPreferencesKey("sip_turn_server")
        val TURN_USERNAME = stringPreferencesKey("sip_turn_username")
        val CODEC_PRIORITY = stringPreferencesKey("sip_codec_priority")
    }

    override val accountPreferences: Flow<SipAccountPreferences?> = context.dataStore.data.map { prefs ->
        val username = prefs[Keys.USERNAME]
        val domain = prefs[Keys.DOMAIN]
        if (username.isNullOrBlank() || domain.isNullOrBlank()) return@map null

        SipAccountPreferences(
            username = username,
            domain = domain,
            port = prefs[Keys.PORT] ?: 5060,
            transport = prefs[Keys.TRANSPORT]?.let { runCatching { SipTransport.valueOf(it) }.getOrNull() }
                ?: SipTransport.UDP,
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            srtpEnabled = prefs[Keys.SRTP_ENABLED] ?: false,
            stunEnabled = prefs[Keys.STUN_ENABLED] ?: false,
            stunServer = prefs[Keys.STUN_SERVER] ?: "",
            iceEnabled = prefs[Keys.ICE_ENABLED] ?: false,
            turnEnabled = prefs[Keys.TURN_ENABLED] ?: false,
            turnServer = prefs[Keys.TURN_SERVER] ?: "",
            turnUsername = prefs[Keys.TURN_USERNAME] ?: "",
            codecPriority = prefs[Keys.CODEC_PRIORITY]?.let(::decodeCodecPriority)
                ?: AudioCodec.DEFAULT_PRIORITY,
        )
    }

    override suspend fun saveAccountPreferences(preferences: SipAccountPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USERNAME] = preferences.username
            prefs[Keys.DOMAIN] = preferences.domain
            prefs[Keys.PORT] = preferences.port
            prefs[Keys.TRANSPORT] = preferences.transport.name
            prefs[Keys.DISPLAY_NAME] = preferences.displayName
            prefs[Keys.SRTP_ENABLED] = preferences.srtpEnabled
            prefs[Keys.STUN_ENABLED] = preferences.stunEnabled
            prefs[Keys.STUN_SERVER] = preferences.stunServer
            prefs[Keys.ICE_ENABLED] = preferences.iceEnabled
            prefs[Keys.TURN_ENABLED] = preferences.turnEnabled
            prefs[Keys.TURN_SERVER] = preferences.turnServer
            prefs[Keys.TURN_USERNAME] = preferences.turnUsername
            prefs[Keys.CODEC_PRIORITY] = encodeCodecPriority(preferences.codecPriority)
        }
    }

    override suspend fun clearAccountPreferences() {
        context.dataStore.edit { it.clear() }
    }

    private fun encodeCodecPriority(codecs: List<AudioCodec>): String = codecs.joinToString(",") { it.name }

    private fun decodeCodecPriority(encoded: String): List<AudioCodec> = encoded.split(",")
        .mapNotNull { runCatching { AudioCodec.valueOf(it) }.getOrNull() }
        .ifEmpty { AudioCodec.DEFAULT_PRIORITY }
}
