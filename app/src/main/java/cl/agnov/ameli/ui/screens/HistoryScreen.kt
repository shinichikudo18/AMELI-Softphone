@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.CallDirection
import cl.agnov.ameli.sip.model.CallHistoryRecord
import cl.agnov.ameli.sip.model.CallResult
import cl.agnov.ameli.ui.viewmodel.HistoryViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    onCallStarted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = ViewModelFactories.history),
) {
    val history by viewModel.history.collectAsState()
    val requestRedial = rememberCallPermissionLauncher<CallHistoryRecord> { record ->
        viewModel.redial(record)
        onCallStarted()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Historial de llamadas") },
                actions = {
                    TextButton(onClick = viewModel::clear) {
                        Text("Borrar")
                    }
                },
            )
        },
    ) { innerPadding ->
        if (history.isEmpty()) {
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Sin llamadas registradas", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(history, key = { it.id }) { record ->
                CallHistoryRow(
                    record = record,
                    onRedial = { requestRedial(record) },
                )
            }
        }
    }
}

@Composable
private fun CallHistoryRow(record: CallHistoryRecord, onRedial: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(record.result.indicatorColor()),
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = record.remoteDisplayName ?: friendlyAddress(record.remoteAddress),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "${record.direction.toDisplayText()} · ${record.result.toDisplayText()} · ${formatDuration(record.durationSeconds)}",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = formatDate(record.startDateEpochSeconds),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onRedial) {
                Text("📞")
            }
        }
    }
}

private fun friendlyAddress(remoteAddress: String): String {
    val withoutScheme = remoteAddress.substringAfter(':', remoteAddress)
    return withoutScheme.substringBefore('@').ifBlank { remoteAddress }
}

private fun CallResult.indicatorColor(): Color = when (this) {
    CallResult.SUCCESS -> Color(0xFF22C55E)
    CallResult.MISSED -> Color(0xFFEF4444)
    CallResult.DECLINED -> Color(0xFFFACC15)
    CallResult.ABORTED -> Color(0xFF94A3B8)
}

private fun formatDate(epochSeconds: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochSeconds * 1000))

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

private fun CallDirection.toDisplayText(): String = when (this) {
    CallDirection.INCOMING -> "Entrante"
    CallDirection.OUTGOING -> "Saliente"
}

private fun CallResult.toDisplayText(): String = when (this) {
    CallResult.SUCCESS -> "Completada"
    CallResult.MISSED -> "Perdida"
    CallResult.DECLINED -> "Rechazada"
    CallResult.ABORTED -> "Cancelada"
}
