@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.SipTransport
import cl.agnov.ameli.ui.viewmodel.SettingsUiState
import cl.agnov.ameli.ui.viewmodel.SettingsViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

@Composable
fun SettingsScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelFactories.settings),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSucceeded) {
        if (uiState.saveSucceeded) onSaved()
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Configuración de cuenta SIP") }) },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.padding(innerPadding).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        SettingsForm(
            uiState = uiState,
            onUsernameChanged = viewModel::onUsernameChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onDomainChanged = viewModel::onDomainChanged,
            onPortChanged = viewModel::onPortChanged,
            onTransportChanged = viewModel::onTransportChanged,
            onDisplayNameChanged = viewModel::onDisplayNameChanged,
            onSrtpEnabledChanged = viewModel::onSrtpEnabledChanged,
            onStunEnabledChanged = viewModel::onStunEnabledChanged,
            onStunServerChanged = viewModel::onStunServerChanged,
            onSave = viewModel::save,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SettingsForm(
    uiState: SettingsUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDomainChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onTransportChanged: (SipTransport) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSrtpEnabledChanged: (Boolean) -> Unit,
    onStunEnabledChanged: (Boolean) -> Unit,
    onStunServerChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var transportMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Ingresa los datos de tu cuenta SIP para registrarte.",
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
            ),
        )

        OutlinedTextField(
            value = uiState.domain,
            onValueChange = onDomainChanged,
            label = { Text("Dominio o servidor SIP") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        OutlinedTextField(
            value = uiState.port,
            onValueChange = onPortChanged,
            label = { Text("Puerto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        )

        ExposedDropdownMenuBox(
            expanded = transportMenuExpanded,
            onExpandedChange = { transportMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = uiState.transport.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Transporte") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = transportMenuExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = transportMenuExpanded,
                onDismissRequest = { transportMenuExpanded = false },
            ) {
                SipTransport.entries.forEach { transport ->
                    DropdownMenuItem(
                        text = { Text(transport.name) },
                        onClick = {
                            onTransportChanged(transport)
                            transportMenuExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text("Nombre para mostrar") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        SwitchRow(
            label = "Cifrado de medios (SRTP)",
            checked = uiState.srtpEnabled,
            onCheckedChange = onSrtpEnabledChanged,
        )

        SwitchRow(
            label = "Usar servidor STUN",
            checked = uiState.stunEnabled,
            onCheckedChange = onStunEnabledChanged,
        )

        if (uiState.stunEnabled) {
            OutlinedTextField(
                value = uiState.stunServer,
                onValueChange = onStunServerChanged,
                label = { Text("Servidor STUN") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        uiState.saveError?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onSave,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (uiState.isSaving) "Guardando..." else "Guardar y registrar")
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
