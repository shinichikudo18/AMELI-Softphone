package cl.agnov.ameli.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Devuelve una función que, al invocarse con un valor [T] (por ejemplo, la
 * dirección a llamar), verifica el permiso RECORD_AUDIO y ejecuta
 * [onGranted] con ese valor — solicitando el permiso primero si hace falta.
 * Reutilizable en cualquier pantalla desde la que se pueda iniciar una
 * llamada (Dialer, Historial, etc.).
 */
@Composable
fun <T> rememberCallPermissionLauncher(onGranted: (T) -> Unit): (T) -> Unit {
    val context = LocalContext.current
    val currentOnGranted by rememberUpdatedState(onGranted)
    var pendingValue by remember { mutableStateOf<Any?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        @Suppress("UNCHECKED_CAST")
        val value = pendingValue as? T
        if (granted && value != null) {
            currentOnGranted(value)
        }
        pendingValue = null
    }

    return { value ->
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            currentOnGranted(value)
        } else {
            pendingValue = value
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

/** Variante sin argumento, para cuando la acción no necesita datos extra. */
@Composable
fun rememberCallPermissionLauncher(onGranted: () -> Unit): () -> Unit {
    val launcher = rememberCallPermissionLauncher<Unit> { onGranted() }
    return { launcher(Unit) }
}
