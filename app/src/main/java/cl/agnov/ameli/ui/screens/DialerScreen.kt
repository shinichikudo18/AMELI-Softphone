@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.agnov.ameli.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.agnov.ameli.sip.model.SipRegistrationState
import cl.agnov.ameli.ui.viewmodel.DialerViewModel
import cl.agnov.ameli.ui.viewmodel.ViewModelFactories

private data class DialPadKey(val digit: Char, val letters: String = "")

private val DIAL_PAD_KEYS = listOf(
    listOf(DialPadKey('1'), DialPadKey('2', "ABC"), DialPadKey('3', "DEF")),
    listOf(DialPadKey('4', "GHI"), DialPadKey('5', "JKL"), DialPadKey('6', "MNO")),
    listOf(DialPadKey('7', "PQRS"), DialPadKey('8', "TUV"), DialPadKey('9', "WXYZ")),
    listOf(DialPadKey('*'), DialPadKey('0', "+"), DialPadKey('#')),
)

@Composable
fun DialerScreen(
    onCallStarted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DialerViewModel = viewModel(factory = ViewModelFactories.dialer),
) {
    val dialedAddress by viewModel.dialedAddress.collectAsState()
    val callError by viewModel.callError.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()

    val startCall = rememberCallPermissionLauncher {
        viewModel.call()
        onCallStarted()
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                textStyle = MaterialTheme.typography.headlineSmall,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DIAL_PAD_KEYS.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        row.forEach { key ->
                            DialPadButton(key = key, onClick = { viewModel.onKeyPressed(key.digit) })
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(64.dp))

                FilledIconButton(
                    onClick = startCall,
                    enabled = registrationState == SipRegistrationState.REGISTERED,
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text("📞", style = MaterialTheme.typography.headlineSmall)
                }

                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (dialedAddress.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = viewModel::onBackspace,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("⌫")
                        }
                    }
                }
            }

            callError?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DialPadButton(key: DialPadKey, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(72.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(key.digit.toString(), style = MaterialTheme.typography.headlineSmall)
            if (key.letters.isNotEmpty()) {
                Text(key.letters, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
