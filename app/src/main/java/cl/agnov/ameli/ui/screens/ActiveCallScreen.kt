package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import cl.agnov.ameli.ui.viewmodel.ActiveCallViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

@Composable
fun ActiveCallScreen(
    onCallEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveCallViewModel = viewModel(factory = ViewModelFactories.activeCall),
) {
    val callState by viewModel.uiState.collectAsState()

    LaunchedEffect(callState?.connectionState) {
        if (callState == null || callState?.connectionState == CallConnectionState.ENDED) {
            onCallEnded()
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
            if (state == null) {
                Text("Sin llamada activa", style = MaterialTheme.typography.titleMedium)
                return@Column
            }

            Text(
                text = state.remoteDisplayName ?: state.remoteAddress,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = state.connectionState.toDisplayMessage(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = formatDuration(state.durationSeconds),
                style = MaterialTheme.typography.titleLarge,
            )

            Button(
                onClick = viewModel::hangup,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Colgar")
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun CallConnectionState.toDisplayMessage(): String = when (this) {
    CallConnectionState.IDLE -> "Preparando llamada…"
    CallConnectionState.OUTGOING_INIT -> "Iniciando llamada…"
    CallConnectionState.OUTGOING_RINGING -> "Llamando…"
    CallConnectionState.INCOMING_RINGING -> "Llamada entrante"
    CallConnectionState.CONNECTED -> "En llamada"
    CallConnectionState.PAUSED -> "En espera"
    CallConnectionState.ENDED -> "Llamada finalizada"
    CallConnectionState.ERROR -> "Error en la llamada"
}
