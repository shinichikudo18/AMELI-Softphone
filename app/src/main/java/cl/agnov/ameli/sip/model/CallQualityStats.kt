package cl.agnov.ameli.sip.model

import org.linphone.core.IceState

/**
 * Estado de conectividad ICE de la llamada, traducido desde
 * [org.linphone.core.IceState].
 */
enum class IceConnectionState {
    NOT_ACTIVATED,
    IN_PROGRESS,
    HOST,
    REFLEXIVE,
    RELAY,
    FAILED,
    ;

    companion object {
        fun from(state: IceState): IceConnectionState = when (state) {
            IceState.NotActivated -> NOT_ACTIVATED
            IceState.InProgress -> IN_PROGRESS
            IceState.HostConnection -> HOST
            IceState.ReflexiveConnection -> REFLEXIVE
            IceState.RelayConnection -> RELAY
            IceState.Failed -> FAILED
        }
    }
}

/**
 * Estadísticas de calidad de la llamada en curso, actualizadas en tiempo
 * real a partir de [org.linphone.core.CallStats]. `jitterSeconds` y
 * `roundTripSeconds` reflejan las unidades de la API nativa (segundos; solo
 * los métodos con sufijo `Ms` de esa API están en milisegundos).
 */
data class CallQualityStats(
    val codecName: String?,
    val packetLossPercent: Float,
    val jitterSeconds: Float,
    val roundTripSeconds: Float,
    val iceState: IceConnectionState,
)
