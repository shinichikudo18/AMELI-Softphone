@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
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

        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            items(history, key = { it.id }) { record ->
                CallHistoryRow(
                    record = record,
                    onRedial = { requestRedial(record) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun CallHistoryRow(record: CallHistoryRecord, onRedial: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = record.remoteDisplayName ?: record.remoteAddress,
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
