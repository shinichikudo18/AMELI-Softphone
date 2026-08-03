@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.SipRegistrationState
import cl.agnov.ameli.ui.viewmodel.DialerViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

private val DIAL_PAD_KEYS = listOf(
    listOf('1', '2', '3'),
    listOf('4', '5', '6'),
    listOf('7', '8', '9'),
    listOf('*', '0', '#'),
)

@Composable
fun DialerScreen(
    onCallStarted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DialerViewModel = viewModel(factory = ViewModelFactories.dialer),
) {
    val context = LocalContext.current
    val dialedAddress by viewModel.dialedAddress.collectAsState()
    val callError by viewModel.callError.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()

    var pendingCall by remember { mutableStateOf(false) }

    val requestAudioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && pendingCall) {
            viewModel.call()
            onCallStarted()
        }
        pendingCall = false
    }

    fun startCall() {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            viewModel.call()
            onCallStarted()
        } else {
            pendingCall = true
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Marcar") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (registrationState != SipRegistrationState.REGISTERED) {
                Text(
                    text = "Necesitas una cuenta SIP registrada para poder llamar.",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            OutlinedTextField(
                value = dialedAddress,
                onValueChange = viewModel::onDialedAddressChanged,
                label = { Text("Número o dirección SIP") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            DIAL_PAD_KEYS.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEach { key ->
                        OutlinedButton(onClick = { viewModel.onKeyPressed(key) }) {
                            Text(key.toString(), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = viewModel::onBackspace, modifier = Modifier.weight(1f)) {
                    Text("Borrar")
                }
                Button(
                    onClick = ::startCall,
                    enabled = registrationState == SipRegistrationState.REGISTERED,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Llamar")
                }
            }

            callError?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
