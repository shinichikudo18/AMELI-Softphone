@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.R
import cl.agnov.ameli.sip.LinphoneManager
import cl.agnov.ameli.sip.model.SipRegistrationState
import cl.agnov.ameli.ui.viewmodel.UpdateViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenDialer: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    updateViewModel: UpdateViewModel = viewModel(factory = ViewModelFactories.update),
) {
    val registrationState by LinphoneManager.registrationState.collectAsState()
    val availableUpdate by updateViewModel.availableUpdate.collectAsState()
    val context = LocalContext.current

    availableUpdate?.let { release ->
        AlertDialog(
            onDismissRequest = updateViewModel::dismiss,
            title = { Text("Nueva versión disponible") },
            text = { Text("Hay una nueva versión (v${release.versionName}) de AMELI Softphone lista para descargar.") },
            confirmButton = {
                TextButton(onClick = {
                    val url = release.apkDownloadUrl ?: release.releaseUrl
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    updateViewModel.dismiss()
                }) {
                    Text("Descargar")
                }
            },
            dismissButton = {
                TextButton(onClick = updateViewModel::dismiss) {
                    Text("Ahora no")
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("AMELI Softphone") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_ameli),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
            )

            RegistrationStatusChip(registrationState)

            Button(
                onClick = onOpenDialer,
                enabled = registrationState == SipRegistrationState.REGISTERED,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Marcar")
            }

            OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Historial de llamadas")
            }

            OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Configurar cuenta SIP")
            }
        }
    }
}

@Composable
private fun RegistrationStatusChip(state: SipRegistrationState) {
    Card(
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(state.indicatorColor()),
            )
            Text(
                text = state.toDisplayMessage(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun SipRegistrationState.indicatorColor(): Color = when (this) {
    SipRegistrationState.REGISTERED -> Color(0xFF22C55E)
    SipRegistrationState.REGISTERING -> Color(0xFFFACC15)
    SipRegistrationState.NOT_REGISTERED, SipRegistrationState.DISCONNECTED -> Color(0xFF94A3B8)
    else -> Color(0xFFEF4444)
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
