package cl.agnov.ameli.update

import android.content.Context
import androidx.core.content.edit

/** Recuerda qué versión de actualización rechazó el usuario, para no insistir con la misma. */
class UpdateDismissalStore(context: Context) {
    private val preferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun isDismissed(versionName: String): Boolean = preferences.getString(KEY_DISMISSED, null) == versionName

    fun dismiss(versionName: String) {
        preferences.edit { putString(KEY_DISMISSED, versionName) }
    }

    private companion object {
        const val FILE_NAME = "ameli_update_prefs"
        const val KEY_DISMISSED = "dismissed_version"
    }
}
