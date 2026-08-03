@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.agnov.ameli.sip.LinphoneManager
import cl.agnov.ameli.sip.model.SipRegistrationState

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenDialer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val registrationState by LinphoneManager.registrationState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("AMELI Softphone") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = registrationState.toDisplayMessage(),
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                onClick = onOpenDialer,
                enabled = registrationState == SipRegistrationState.REGISTERED,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Marcar")
            }

            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Configurar cuenta SIP")
            }
        }
    }
}

private fun SipRegistrationState.toDisplayMessage(): String = when (this) {
    SipRegistrationState.NOT_REGISTERED -> "Sin cuenta SIP configurada. Configúrala para empezar a llamar."
    SipRegistrationState.REGISTERING -> "Registrando cuenta SIP…"
    SipRegistrationState.REGISTERED -> "Cuenta SIP registrada"
    SipRegistrationState.DISCONNECTED -> "Desconectado del servidor SIP"
    SipRegistrationState.AUTHENTICATION_ERROR -> "Error de autenticación: revisa usuario y contraseña"
    SipRegistrationState.SERVER_UNAVAILABLE -> "Servidor SIP no disponible"
    SipRegistrationState.CERTIFICATE_ERROR -> "Error de certificado TLS del servidor"
    SipRegistrationState.UNKNOWN_ERROR -> "No se pudo registrar la cuenta SIP"
}
