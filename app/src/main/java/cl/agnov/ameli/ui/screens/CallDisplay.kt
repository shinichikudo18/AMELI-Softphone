package cl.agnov.ameli.ui.screens

import cl.agnov.ameli.sip.model.CallUiState

/**
 * Nombre para mostrar si existe; si no, el número/usuario SIP en vez de la
 * URI cruda (p.ej. "203" en lugar de "sip:203@192.168.1.1:5060").
 */
fun friendlyCallerLabel(state: CallUiState): String {
    state.remoteDisplayName?.takeIf { it.isNotBlank() }?.let { return it }
    val withoutScheme = state.remoteAddress.substringAfter(':', state.remoteAddress)
    return withoutScheme.substringBefore('@').ifBlank { state.remoteAddress }
}
