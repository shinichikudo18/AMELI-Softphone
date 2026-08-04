package cl.agnov.ameli.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "ameli_theme_preferences")

/** Permite sustituir [ThemePreferenceRepository] por un fake en pruebas unitarias. */
interface ThemePreferenceStore {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

/** Preferencia de tema (sistema/claro/oscuro), independiente del color de marca. */
class ThemePreferenceRepository(private val context: Context) : ThemePreferenceStore {

    private val key = stringPreferencesKey("theme_mode")

    override val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[key]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { it[key] = mode.name }
    }
}
