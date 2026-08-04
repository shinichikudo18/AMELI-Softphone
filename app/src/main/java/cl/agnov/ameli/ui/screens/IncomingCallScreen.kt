package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.ui.viewmodel.IncomingCallViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

@Composable
fun IncomingCallScreen(
    onAnswered: () -> Unit,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingCallViewModel = viewModel(factory = ViewModelFactories.incomingCall),
) {
    val callState by viewModel.callState.collectAsState()
    val isRingerSilenced by viewModel.isRingerSilenced.collectAsState()

    LaunchedEffect(callState?.connectionState) {
        when (callState?.connectionState) {
            CallConnectionState.CONNECTED -> onAnswered()
            CallConnectionState.ENDED, null -> onDismissed()
            else -> Unit
        }
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val state = callState

            Text(
                text = "Llamada entrante",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = state?.remoteDisplayName ?: state?.remoteAddress ?: "",
                style = MaterialTheme.typography.headlineSmall,
            )

            if (!isRingerSilenced) {
                OutlinedButton(
                    onClick = viewModel::silenceRinger,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Silenciar timbre")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::decline,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Rechazar")
                }
                Button(
                    onClick = viewModel::answer,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Contestar")
                }
            }
        }
    }
}
