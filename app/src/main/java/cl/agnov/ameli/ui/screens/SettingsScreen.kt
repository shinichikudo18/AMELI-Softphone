@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import cl.agnov.ameli.data.ThemeMode
import cl.agnov.ameli.sip.model.AudioCodec
import cl.agnov.ameli.sip.model.NetworkProfile
import cl.agnov.ameli.sip.model.SipTransport
import cl.agnov.ameli.ui.viewmodel.SettingsUiState
import cl.agnov.ameli.ui.viewmodel.SettingsViewModel
import cl.agnov.ameli.ui.viewmodel.ThemeViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

@Composable
fun SettingsScreen(
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelFactories.settings),
    themeViewModel: ThemeViewModel = viewModel(factory = ViewModelFactories.theme),
) {
    val uiState by viewModel.uiState.collectAsState()
    val networkProfiles by viewModel.networkProfiles.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsState()

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
            themeMode = themeMode,
            onThemeModeChanged = themeViewModel::setThemeMode,
            networkProfiles = networkProfiles,
            onSaveNetworkProfile = viewModel::saveCurrentAsNetworkProfile,
            onApplyNetworkProfile = viewModel::applyNetworkProfile,
            onDeleteNetworkProfile = viewModel::deleteNetworkProfile,
            onUsernameChanged = viewModel::onUsernameChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onDomainChanged = viewModel::onDomainChanged,
            onPortChanged = viewModel::onPortChanged,
            onTransportChanged = viewModel::onTransportChanged,
            onDisplayNameChanged = viewModel::onDisplayNameChanged,
            onSrtpEnabledChanged = viewModel::onSrtpEnabledChanged,
            onStunEnabledChanged = viewModel::onStunEnabledChanged,
            onStunServerChanged = viewModel::onStunServerChanged,
            onIceEnabledChanged = viewModel::onIceEnabledChanged,
            onTurnEnabledChanged = viewModel::onTurnEnabledChanged,
            onTurnServerChanged = viewModel::onTurnServerChanged,
            onTurnUsernameChanged = viewModel::onTurnUsernameChanged,
            onTurnPasswordChanged = viewModel::onTurnPasswordChanged,
            onCodecToggled = viewModel::onCodecToggled,
            onCodecMoved = viewModel::onCodecMoved,
            onAgcEnabledChanged = viewModel::onAgcEnabledChanged,
            onNoiseSuppressionEnabledChanged = viewModel::onNoiseSuppressionEnabledChanged,
            onEchoCancellationEnabledChanged = viewModel::onEchoCancellationEnabledChanged,
            onMicGainDbChanged = viewModel::onMicGainDbChanged,
            onPlaybackGainDbChanged = viewModel::onPlaybackGainDbChanged,
            onSave = viewModel::save,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SettingsForm(
    uiState: SettingsUiState,
    themeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    networkProfiles: List<NetworkProfile>,
    onSaveNetworkProfile: (String) -> Unit,
    onApplyNetworkProfile: (NetworkProfile) -> Unit,
    onDeleteNetworkProfile: (NetworkProfile) -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDomainChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onTransportChanged: (SipTransport) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSrtpEnabledChanged: (Boolean) -> Unit,
    onStunEnabledChanged: (Boolean) -> Unit,
    onStunServerChanged: (String) -> Unit,
    onIceEnabledChanged: (Boolean) -> Unit,
    onTurnEnabledChanged: (Boolean) -> Unit,
    onTurnServerChanged: (String) -> Unit,
    onTurnUsernameChanged: (String) -> Unit,
    onTurnPasswordChanged: (String) -> Unit,
    onCodecToggled: (AudioCodec, Boolean) -> Unit,
    onCodecMoved: (AudioCodec, Int) -> Unit,
    onAgcEnabledChanged: (Boolean) -> Unit,
    onNoiseSuppressionEnabledChanged: (Boolean) -> Unit,
    onEchoCancellationEnabledChanged: (Boolean) -> Unit,
    onMicGainDbChanged: (Float) -> Unit,
    onPlaybackGainDbChanged: (Float) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var transportMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ingresa los datos de tu cuenta SIP para registrarte.",
            style = MaterialTheme.typography.bodyMedium,
        )

        SettingsSection(title = "Cuenta SIP") {
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
        }

        SettingsSection(title = "NAT y conectividad") {
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

            SwitchRow(
                label = "Usar ICE (mejor traversal de NAT)",
                checked = uiState.iceEnabled,
                onCheckedChange = onIceEnabledChanged,
            )

            SwitchRow(
                label = "Usar servidor TURN",
                checked = uiState.turnEnabled,
                onCheckedChange = onTurnEnabledChanged,
            )

            if (uiState.turnEnabled) {
                OutlinedTextField(
                    value = uiState.turnServer,
                    onValueChange = onTurnServerChanged,
                    label = { Text("Servidor TURN (host:puerto)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.turnUsername,
                    onValueChange = onTurnUsernameChanged,
                    label = { Text("Usuario TURN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.turnPassword,
                    onValueChange = onTurnPasswordChanged,
                    label = { Text("Contraseña TURN") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            }
        }

        NetworkProfilesSection(
            profiles = networkProfiles,
            onSaveCurrent = onSaveNetworkProfile,
            onApply = onApplyNetworkProfile,
            onDelete = onDeleteNetworkProfile,
        )

        SettingsSection(title = "Prioridad de códecs de audio") {
            Text(
                text = "Desmarca los que no quieras usar; el orden de la lista es la prioridad.",
                style = MaterialTheme.typography.bodySmall,
            )

            // El orden visual debe reflejar uiState.codecPriority (lo que el
            // usuario reordenó), no el orden fijo de declaración del enum;
            // los códecs desmarcados se listan al final.
            val orderedCodecs = uiState.codecPriority + AudioCodec.entries.filterNot { it in uiState.codecPriority }
            orderedCodecs.forEach { codec ->
                CodecRow(
                    codec = codec,
                    enabled = codec in uiState.codecPriority,
                    onToggle = { onCodecToggled(codec, it) },
                    onMoveUp = { onCodecMoved(codec, -1) },
                    onMoveDown = { onCodecMoved(codec, 1) },
                )
            }
        }

        SettingsSection(title = "Avanzado: audio") {
            Text(
                text = "Si el micrófono se escucha bajo, activa el control automático de " +
                    "ganancia o sube la ganancia manual. La mayoría de los usuarios no " +
                    "necesita tocar esto.",
                style = MaterialTheme.typography.bodySmall,
            )

            SwitchRow(
                label = "Control automático de ganancia (AGC)",
                checked = uiState.agcEnabled,
                onCheckedChange = onAgcEnabledChanged,
            )
            SwitchRow(
                label = "Supresión de ruido",
                checked = uiState.noiseSuppressionEnabled,
                onCheckedChange = onNoiseSuppressionEnabledChanged,
            )
            SwitchRow(
                label = "Cancelación de eco",
                checked = uiState.echoCancellationEnabled,
                onCheckedChange = onEchoCancellationEnabledChanged,
            )

            GainSliderRow(
                label = "Ganancia del micrófono",
                valueDb = uiState.micGainDb,
                onValueChange = onMicGainDbChanged,
            )
            GainSliderRow(
                label = "Ganancia de reproducción (altavoz/auricular)",
                valueDb = uiState.playbackGainDb,
                onValueChange = onPlaybackGainDbChanged,
            )
        }

        SettingsSection(title = "Apariencia") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeModeOption(
                    label = "Sistema",
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChanged(ThemeMode.SYSTEM) },
                )
                ThemeModeOption(
                    label = "Claro",
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChanged(ThemeMode.LIGHT) },
                )
                ThemeModeOption(
                    label = "Oscuro",
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChanged(ThemeMode.DARK) },
                )
            }
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
private fun NetworkProfilesSection(
    profiles: List<NetworkProfile>,
    onSaveCurrent: (String) -> Unit,
    onApply: (NetworkProfile) -> Unit,
    onDelete: (NetworkProfile) -> Unit,
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    SettingsSection(title = "Perfiles de red") {
        Text(
            text = "Guarda la configuración de NAT/códec de arriba como un perfil " +
                "(p.ej. \"Wi-Fi casa\", \"Datos móviles\", \"Oficina\") y aplícala con un toque " +
                "al cambiar de red.",
            style = MaterialTheme.typography.bodySmall,
        )

        if (profiles.isEmpty()) {
            Text(text = "Sin perfiles guardados", style = MaterialTheme.typography.bodySmall)
        } else {
            profiles.forEach { profile ->
                NetworkProfileRow(
                    profile = profile,
                    onApply = { onApply(profile) },
                    onDelete = { onDelete(profile) },
                )
            }
        }

        OutlinedButton(onClick = { showSaveDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar configuración actual como perfil")
        }
    }

    if (showSaveDialog) {
        SaveProfileDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                onSaveCurrent(name)
                showSaveDialog = false
            },
        )
    }
}

@Composable
private fun NetworkProfileRow(profile: NetworkProfile, onApply: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = profile.summary(),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onApply) {
                Text("Aplicar")
            }
            IconButton(onClick = onDelete) {
                Text("🗑")
            }
        }
    }
}

private fun NetworkProfile.summary(): String = listOfNotNull(
    if (stunEnabled) "STUN" else null,
    if (iceEnabled) "ICE" else null,
    if (turnEnabled) "TURN" else null,
).ifEmpty { listOf("sin NAT") }.joinToString(" · ")

@Composable
private fun SaveProfileDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo perfil de red") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre (p.ej. Wi-Fi casa)") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }, enabled = name.isNotBlank()) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}

@Composable
private fun RowScope.ThemeModeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.weight(1f)) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.weight(1f)) {
            Text(label)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            content()
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

@Composable
private fun GainSliderRow(label: String, valueDb: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(
            text = "$label: %.0f dB".format(valueDb),
            style = MaterialTheme.typography.bodyLarge,
        )
        Slider(
            value = valueDb,
            onValueChange = onValueChange,
            valueRange = -24f..24f,
            steps = 47,
        )
    }
}

@Composable
private fun CodecRow(
    codec: AudioCodec,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = enabled, onCheckedChange = onToggle)
                Text(text = codec.name)
            }
            Row {
                IconButton(onClick = onMoveUp, enabled = enabled) {
                    Text("▲")
                }
                IconButton(onClick = onMoveDown, enabled = enabled) {
                    Text("▼")
                }
            }
        }
    }
}
