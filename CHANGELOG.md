# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
Este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Añadido

- Mejoras de calidad de llamada y NAT: ICE independiente de STUN, soporte de
  servidor TURN (con credenciales cifradas igual que la contraseña SIP) y
  prioridad de códecs de audio configurable (`Core.setAudioPayloadTypes`).
  Estadísticas de llamada en tiempo real (códec en uso, pérdida de paquetes,
  jitter, RTT, estado ICE) vía `CoreListenerStub.onCallStatsUpdated`,
  mostradas en la pantalla de llamada activa.
- Confiabilidad de conexión: `LinphoneManager` reintenta el registro SIP con
  backoff exponencial (2s→60s) cuando falla por servidor no disponible o
  error desconocido; no reintenta ante errores de autenticación o
  certificado, que requieren corrección manual.

## [0.1.1]

### Añadido

- Identidad visual de marca: ícono adaptativo generado a partir del logo
  oficial de AMELI (fondo navy `#0A1A33` + el avatar circular como
  foreground, escalado a la "safe zone" de 66/108dp para no recortarse en
  ninguna forma de máscara). Paleta de la app (`Color.kt`/`Theme.kt`)
  actualizada a navy/cian de marca, con `dynamicColor` desactivado por
  defecto para que Android 12+ no la reemplace por los colores dinámicos
  del fondo de pantalla. El logo se muestra también dentro de la app en la
  pantalla principal, junto a un indicador de estado de registro con
  semáforo de color.

- Fase 3: registro de cuenta SIP. `SipAccountManager` construye
  `AccountParams`/`Account`/`AuthInfo` reales y los registra en el `Core`.
  `PreferencesRepository` (DataStore) persiste la configuración no sensible;
  `SecureCredentialStore` cifra la contraseña con una clave AES-GCM en
  Android Keystore (no se usa `androidx.security.crypto`, deprecado desde su
  1.1.0). Pantallas de inicio y configuración con Jetpack Navigation Compose.
  `SettingsViewModel` con pruebas unitarias usando fakes de las interfaces
  `AccountPreferencesStore`, `CredentialStore` y `AccountConfigurator`.
- Fase 4: llamadas salientes. `CallManager` construye la dirección SIP con
  `Account.normalizeSipUri`, inicia la llamada con `Core.inviteAddress` y
  expone el estado en tiempo real (`CallUiState`) a partir de los callbacks
  de `LinphoneManager`. Pantallas Dialer (teclado numérico) y Llamada activa
  (nombre remoto, duración en vivo, colgar). Se solicita el permiso
  `RECORD_AUDIO` justo antes de la primera llamada.
- Fase 5: llamadas entrantes. `CallManager` gana `answer()`/`decline()`.
  `CallNotificationController` observa el estado de llamada y muestra una
  notificación de categoría CALL con pantalla completa (para desbloquear el
  dispositivo) y acciones directas de contestar/rechazar vía
  `IncomingCallActionReceiver`, sin necesidad de abrir la app. Pantalla
  `IncomingCallScreen` y navegación automática hacia ella cuando llega una
  llamada. Permisos `POST_NOTIFICATIONS` (Android 13+) y
  `USE_FULL_SCREEN_INTENT`; `MainActivity` declara `showWhenLocked`/
  `turnScreenOn` (API 27+, sin efecto pero inofensivo en API 26).
- Fase 6: audio en llamada activa. `AudioRouteManager` controla el silencio
  del micrófono (`Core.isMicEnabled`) y la ruta de salida (`Core.audioDevices`
  / `Core.outputAudioDevice`, alternando entre `AudioDevice.Type.Speaker` y
  `Earpiece`). `CallManager` gana `toggleMute()`, `toggleSpeaker()` y
  `sendDtmf()` (`Call.sendDtmf`). `ActiveCallScreen` añade botones de
  silenciar/altavoz y un teclado DTMF colapsable.
- Fase 7: segundo plano. `LinphoneService` (foreground service,
  `foregroundServiceType="phoneCall"`) pasa a ser el único responsable de
  arrancar/detener `LinphoneManager` (antes lo hacía `AmeliApplication`
  directamente, como se dejó anotado desde la Fase 2). Canal de notificación
  de baja importancia para el servicio, separado del canal de llamadas
  entrantes. Se registra un `ConnectivityManager.NetworkCallback` que llama a
  `Core.refreshRegisters()` cuando vuelve la red. Permisos
  `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_PHONE_CALL` y `MANAGE_OWN_CALLS`
  (este último requerido junto al tipo `phoneCall` al no ser la app un
  marcador/dialer por defecto).
- Fase 8: historial y seguridad. `CallHistoryRepository`/`RoomCallHistoryRepository`
  (Room, con KSP) guardan número remoto, dirección, fecha, duración y
  resultado de cada llamada (`Call.CallLog`/`Call.Status` reales) al llegar
  a `Call.State.Released`. Pantalla de historial con opción de borrar.
  Revisión de seguridad: sin `Log`/`println` de datos sensibles, sin
  `TrustManager`/`HostnameVerifier` personalizados que debiliten TLS, sin
  credenciales hardcodeadas, `local.properties` confirmado fuera de git.
- Fase 9: CI/CD y documentación final. Workflow de GitHub Actions
  (`.github/workflows/android-ci.yml`): build + tests + lint en cada push y
  pull request a `main`, con el APK de debug publicado como artifact; en
  cada tag `vX.Y.Z` compila un APK de release y crea una GitHub Release
  adjuntándolo. La firma de release se lee desde GitHub Secrets
  (`RELEASE_KEYSTORE_BASE64` y relacionados) sin imprimir su contenido en
  los logs, con fallback a la firma de debug si no están configurados (para
  que `assembleRelease` funcione en local sin secretos reales). Documentación
  de compilación actualizada con instrucciones para descargar un APK ya
  compilado y para configurar la firma de release.

- Fase 1: proyecto base compilable — módulo `app` con Kotlin, Jetpack Compose
  (Material 3), tema propio, pantalla de bienvenida y estructura MVVM inicial.
- Configuración de Gradle (Kotlin DSL) con Gradle 9.5.0 y Android Gradle
  Plugin 9.3.1, `compileSdk`/`targetSdk` 36, `minSdk` 26.
- Documentación base del repositorio: README, LICENSE (AGPLv3), CONTRIBUTING,
  SECURITY, `.gitignore` y `local.properties.example`.

## [0.1.0] - Sin publicar

Versión inicial en desarrollo. Aún no cuenta con integración SIP funcional.
