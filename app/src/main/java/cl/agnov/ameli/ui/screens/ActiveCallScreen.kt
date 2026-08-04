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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.sip.model.CallQualityStats
import cl.agnov.ameli.sip.model.IceConnectionState
import cl.agnov.ameli.ui.viewmodel.ActiveCallViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

private val DTMF_KEYS = listOf(
    listOf('1', '2', '3'),
    listOf('4', '5', '6'),
    listOf('7', '8', '9'),
    listOf('*', '0', '#'),
)

@Composable
fun ActiveCallScreen(
    onCallEnded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActiveCallViewModel = viewModel(factory = ViewModelFactories.activeCall),
) {
    val callState by viewModel.uiState.collectAsState()
    val qualityStats by viewModel.qualityStats.collectAsState()
    var showDialpad by remember { mutableStateOf(false) }

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

            qualityStats?.let { QualityStatsRow(it) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = viewModel::toggleMute, modifier = Modifier.weight(1f)) {
                    Text(if (state.isMicMuted) "Reactivar mic" else "Silenciar")
                }
                OutlinedButton(onClick = viewModel::toggleSpeaker, modifier = Modifier.weight(1f)) {
                    Text(if (state.isSpeakerOn) "Altavoz activado" else "Altavoz")
                }
            }

            OutlinedButton(
                onClick = { showDialpad = !showDialpad },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (showDialpad) "Ocultar teclado" else "Teclado DTMF")
            }

            if (showDialpad) {
                DTMF_KEYS.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { key ->
                            OutlinedButton(onClick = { viewModel.sendDtmf(key) }) {
                                Text(key.toString(), style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = viewModel::hangup,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Colgar")
            }
        }
    }
}

@Composable
private fun QualityStatsRow(stats: CallQualityStats) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = listOfNotNull(
                stats.codecName?.let { "Códec: $it" },
                "Pérdida: %.1f%%".format(stats.packetLossPercent),
                "Jitter: %.3fs".format(stats.jitterSeconds),
                "RTT: %.3fs".format(stats.roundTripSeconds),
            ).joinToString("  ·  "),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "ICE: ${stats.iceState.toDisplayText()}",
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun IceConnectionState.toDisplayText(): String = when (this) {
    IceConnectionState.NOT_ACTIVATED -> "no activo"
    IceConnectionState.IN_PROGRESS -> "negociando…"
    IceConnectionState.HOST -> "directo"
    IceConnectionState.REFLEXIVE -> "vía STUN"
    IceConnectionState.RELAY -> "vía TURN (relay)"
    IceConnectionState.FAILED -> "falló"
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
