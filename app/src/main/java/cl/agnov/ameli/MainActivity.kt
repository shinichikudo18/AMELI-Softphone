package cl.agnov.ameli

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.data.ThemeMode
import cl.agnov.ameli.ui.navigation.AmeliNavHost
import cl.agnov.ameli.ui.theme.AmeliTheme
import cl.agnov.ameli.ui.viewmodel.ThemeViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = ViewModelFactories.theme)
            val themeMode by themeViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AmeliTheme(darkTheme = darkTheme) {
                RequestNotificationPermissionIfNeeded()
                RequestBluetoothConnectPermissionIfNeeded()
                AmeliNavHost()
            }
        }
    }
}

/**
 * Solicita el permiso POST_NOTIFICATIONS (Android 13+) para poder mostrar la
 * notificación de llamada entrante. En versiones anteriores el permiso no
 * existe y se concede implícitamente.
 */
@Composable
private fun RequestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* El usuario decide; sin notificación no verá la llamada entrante, pero puede contestar desde la app abierta. */ }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

/**
 * Solicita el permiso BLUETOOTH_CONNECT (Android 12+) para poder listar y
 * enrutar audio hacia dispositivos Bluetooth durante una llamada. En
 * versiones anteriores el permiso no existe y se concede implícitamente.
 */
@Composable
private fun RequestBluetoothConnectPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* Sin este permiso simplemente no aparecerá la opción de ruta Bluetooth. */ }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }
}
