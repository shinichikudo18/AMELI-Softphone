package cl.agnov.ameli.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.AudioRoute
import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.sip.model.CallQualityStats
import cl.agnov.ameli.sip.model.CallUiState
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
    val secondaryCallState by viewModel.secondaryCallState.collectAsState()
    val qualityStats by viewModel.qualityStats.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val isConferenceActive by viewModel.isConferenceActive.collectAsState()
    val conferenceParticipants by viewModel.conferenceParticipants.collectAsState()
    var showDialpad by remember { mutableStateOf(false) }
    var showSecondCallInput by remember { mutableStateOf(false) }
    var showTransferInput by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    var showAddParticipantInput by remember { mutableStateOf(false) }

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

            CallerAvatar(label = friendlyCallerLabel(state))

            Text(
                text = friendlyCallerLabel(state),
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

            TextButton(onClick = { showDetails = !showDetails }) {
                Text(if (showDetails) "Ocultar detalles de llamada" else "Detalles de llamada")
            }

            if (showDetails) {
                CallDetailsCard(state = state, stats = qualityStats)
            }

            if (isConferenceActive) {
                ConferenceCard(
                    participants = conferenceParticipants,
                    onHangupParticipant = viewModel::hangupParticipant,
                    onEndConference = viewModel::endConference,
                    showAddParticipantInput = showAddParticipantInput,
                    onToggleAddParticipant = { showAddParticipantInput = !showAddParticipantInput },
                    onAddParticipant = {
                        viewModel.addConferenceParticipant(it)
                        showAddParticipantInput = false
                    },
                )
            } else {
                secondaryCallState?.let { secondary ->
                    SecondaryCallCard(
                        secondary = secondary,
                        onAnswer = viewModel::answerSecondary,
                        onDecline = viewModel::declineSecondary,
                        onSwap = viewModel::swapCalls,
                        onHangup = viewModel::hangupSecondary,
                        onMerge = viewModel::completeConsultativeTransfer,
                        onStartConference = viewModel::startConference,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = viewModel::toggleMute, modifier = Modifier.weight(1f)) {
                    Text(if (state.isMicMuted) "Reactivar mic" else "Silenciar")
                }
            }

            AudioRoutePicker(
                current = state.audioRoute,
                available = state.availableAudioRoutes,
                onSelect = viewModel::setAudioRoute,
            )

            if (secondaryCallState == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            showSecondCallInput = !showSecondCallInput
                            showTransferInput = false
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Segunda llamada")
                    }
                    OutlinedButton(
                        onClick = {
                            showTransferInput = !showTransferInput
                            showSecondCallInput = false
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Transferir")
                    }
                }
            }

            if (showSecondCallInput) {
                AddressInputRow(
                    label = "Llamar a",
                    confirmLabel = "Llamar",
                    onConfirm = {
                        viewModel.startSecondCall(it)
                        showSecondCallInput = false
                    },
                )
            }

            if (showTransferInput) {
                AddressInputRow(
                    label = "Transferir a",
                    confirmLabel = "Transferir",
                    onConfirm = {
                        viewModel.transferForegroundTo(it)
                        showTransferInput = false
                    },
                )
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

            actionError?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
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
private fun CallerAvatar(label: String) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.take(1).uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun AudioRoutePicker(
    current: AudioRoute,
    available: List<AudioRoute>,
    onSelect: (AudioRoute) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        available.forEach { route ->
            val selected = route == current
            if (selected) {
                Button(onClick = { onSelect(route) }, modifier = Modifier.weight(1f)) {
                    Text(route.toDisplayLabel())
                }
            } else {
                OutlinedButton(onClick = { onSelect(route) }, modifier = Modifier.weight(1f)) {
                    Text(route.toDisplayLabel())
                }
            }
        }
    }
}

private fun AudioRoute.toDisplayLabel(): String = when (this) {
    AudioRoute.EARPIECE -> "Auricular"
    AudioRoute.SPEAKER -> "Altavoz"
    AudioRoute.BLUETOOTH -> "Bluetooth"
}

@Composable
private fun AddressInputRow(label: String, confirmLabel: String, onConfirm: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        Button(onClick = { if (value.isNotBlank()) onConfirm(value.trim()) }) {
            Text(confirmLabel)
        }
    }
}

@Composable
private fun CallDetailsCard(state: CallUiState, stats: CallQualityStats?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(text = "Dirección SIP: ${state.remoteAddress}", style = MaterialTheme.typography.labelSmall)
            if (stats != null) {
                HorizontalDivider()
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
                    text = "Conexión: ${stats.iceState.toDisplayText()}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun SecondaryCallCard(
    secondary: CallUiState,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onSwap: () -> Unit,
    onHangup: () -> Unit,
    onMerge: () -> Unit,
    onStartConference: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = friendlyCallerLabel(secondary),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = secondary.connectionState.toDisplayMessage(),
                style = MaterialTheme.typography.bodySmall,
            )
            HorizontalDivider()

            if (secondary.connectionState == CallConnectionState.INCOMING_RINGING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f)) {
                        Text("Rechazar")
                    }
                    Button(onClick = onAnswer, modifier = Modifier.weight(1f)) {
                        Text("Contestar")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = onSwap, modifier = Modifier.weight(1f)) {
                        Text("Intercambiar")
                    }
                    OutlinedButton(onClick = onHangup, modifier = Modifier.weight(1f)) {
                        Text("Colgar esta")
                    }
                }
                OutlinedButton(onClick = onMerge, modifier = Modifier.fillMaxWidth()) {
                    Text("Unir llamadas")
                }
                OutlinedButton(onClick = onStartConference, modifier = Modifier.fillMaxWidth()) {
                    Text("Iniciar conferencia")
                }
            }
        }
    }
}

@Composable
private fun ConferenceCard(
    participants: List<CallUiState>,
    onHangupParticipant: (String) -> Unit,
    onEndConference: () -> Unit,
    showAddParticipantInput: Boolean,
    onToggleAddParticipant: () -> Unit,
    onAddParticipant: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Conferencia · ${participants.size} participante(s)",
                style = MaterialTheme.typography.bodyLarge,
            )
            HorizontalDivider()

            participants.forEach { participant ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = friendlyCallerLabel(participant), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = participant.connectionState.toDisplayMessage(),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { onHangupParticipant(participant.callId) }) {
                        Text("Colgar")
                    }
                }
            }

            OutlinedButton(onClick = onToggleAddParticipant, modifier = Modifier.fillMaxWidth()) {
                Text(if (showAddParticipantInput) "Cancelar" else "Agregar participante")
            }

            if (showAddParticipantInput) {
                AddressInputRow(
                    label = "Llamar a",
                    confirmLabel = "Llamar",
                    onConfirm = onAddParticipant,
                )
            }

            Button(onClick = onEndConference, modifier = Modifier.fillMaxWidth()) {
                Text("Finalizar conferencia")
            }
        }
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
