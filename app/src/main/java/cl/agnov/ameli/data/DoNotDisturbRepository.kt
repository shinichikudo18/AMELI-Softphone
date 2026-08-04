package cl.agnov.ameli.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dndDataStore by preferencesDataStore(name = "ameli_app_settings")

/** Permite sustituir [DoNotDisturbRepository] por un fake en pruebas unitarias. */
interface DoNotDisturbState {
    val isEnabled: Flow<Boolean>
    suspend fun setEnabled(enabled: Boolean)
}

/**
 * Modo "No Molestar": mientras está activo, [cl.agnov.ameli.sip.CallManager]
 * rechaza automáticamente las llamadas entrantes en vez de timbrar.
 */
class DoNotDisturbRepository(private val context: Context) : DoNotDisturbState {

    private val key = booleanPreferencesKey("do_not_disturb_enabled")

    override val isEnabled: Flow<Boolean> = context.dndDataStore.data.map { it[key] ?: false }

    override suspend fun setEnabled(enabled: Boolean) {
        context.dndDataStore.edit { it[key] = enabled }
    }
}
