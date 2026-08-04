package cl.agnov.ameli.sip

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import cl.agnov.ameli.sip.model.AudioRoute
import org.linphone.core.AudioDevice

/** Permite sustituir [AudioRouteManager] por un fake en pruebas unitarias. */
interface AudioRouteController {
    fun isMicMuted(): Boolean
    fun setMicMuted(muted: Boolean)
    fun currentRoute(): AudioRoute
    fun availableRoutes(): List<AudioRoute>
    fun setRoute(route: AudioRoute)
}

/**
 * Controla el silencio del micrófono y la ruta de entrada/salida de audio
 * (auricular/altavoz/Bluetooth) del [LinphoneManager.core].
 */
class AudioRouteManager(private val context: Context) : AudioRouteController {

    override fun isMicMuted(): Boolean = !LinphoneManager.core.isMicEnabled

    override fun setMicMuted(muted: Boolean) {
        LinphoneManager.core.isMicEnabled = !muted
    }

    override fun currentRoute(): AudioRoute = when (LinphoneManager.core.outputAudioDevice?.type) {
        AudioDevice.Type.Speaker -> AudioRoute.SPEAKER
        AudioDevice.Type.Bluetooth, AudioDevice.Type.BluetoothA2DP -> AudioRoute.BLUETOOTH
        else -> AudioRoute.EARPIECE
    }

    override fun availableRoutes(): List<AudioRoute> {
        val devices = LinphoneManager.core.audioDevices
        val routes = mutableListOf<AudioRoute>()

        if (devices.any { it.type == AudioDevice.Type.Earpiece }) routes.add(AudioRoute.EARPIECE)
        if (devices.any { it.type == AudioDevice.Type.Speaker }) routes.add(AudioRoute.SPEAKER)
        if (hasBluetoothConnectPermission() &&
            devices.any { it.type == AudioDevice.Type.Bluetooth || it.type == AudioDevice.Type.BluetoothA2DP }
        ) {
            routes.add(AudioRoute.BLUETOOTH)
        }

        return routes.ifEmpty { listOf(AudioRoute.EARPIECE) }
    }

    override fun setRoute(route: AudioRoute) {
        val core = LinphoneManager.core
        when (route) {
            AudioRoute.EARPIECE -> {
                deviceOfType(AudioDevice.Type.Earpiece)?.let { core.outputAudioDevice = it }
                deviceOfType(AudioDevice.Type.Microphone)?.let { core.inputAudioDevice = it }
            }
            AudioRoute.SPEAKER -> {
                deviceOfType(AudioDevice.Type.Speaker)?.let { core.outputAudioDevice = it }
                deviceOfType(AudioDevice.Type.Microphone)?.let { core.inputAudioDevice = it }
            }
            AudioRoute.BLUETOOTH -> {
                // Un dispositivo Bluetooth de llamada (SCO) es bidireccional:
                // se enruta tanto la entrada como la salida hacia él.
                val bluetoothDevice = deviceOfType(AudioDevice.Type.Bluetooth, AudioDevice.Type.BluetoothA2DP)
                bluetoothDevice?.let {
                    core.outputAudioDevice = it
                    core.inputAudioDevice = it
                }
            }
        }
    }

    private fun deviceOfType(vararg types: AudioDevice.Type): AudioDevice? =
        LinphoneManager.core.audioDevices.firstOrNull { it.type in types }

    /** BLUETOOTH_CONNECT es un permiso peligroso desde Android 12 (API 31); antes no existe/es normal. */
    private fun hasBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
