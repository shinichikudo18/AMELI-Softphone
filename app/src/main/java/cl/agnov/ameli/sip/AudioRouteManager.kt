package cl.agnov.ameli.sip

import org.linphone.core.AudioDevice

/** Permite sustituir [AudioRouteManager] por un fake en pruebas unitarias. */
interface AudioRouteController {
    fun isMicMuted(): Boolean
    fun setMicMuted(muted: Boolean)
    fun isSpeakerOn(): Boolean
    fun setSpeakerEnabled(enabled: Boolean)
}

/**
 * Controla el silencio del micrófono y la ruta de salida de audio
 * (altavoz/auricular) del [LinphoneManager.core].
 */
class AudioRouteManager : AudioRouteController {

    override fun isMicMuted(): Boolean = !LinphoneManager.core.isMicEnabled

    override fun setMicMuted(muted: Boolean) {
        LinphoneManager.core.isMicEnabled = !muted
    }

    override fun isSpeakerOn(): Boolean =
        LinphoneManager.core.outputAudioDevice?.type == AudioDevice.Type.Speaker

    override fun setSpeakerEnabled(enabled: Boolean) {
        val core = LinphoneManager.core
        val targetType = if (enabled) AudioDevice.Type.Speaker else AudioDevice.Type.Earpiece
        val device = core.audioDevices.firstOrNull { it.type == targetType } ?: return
        core.outputAudioDevice = device
    }
}
