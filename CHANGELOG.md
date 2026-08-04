# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
Este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [0.2.3]

### Añadido

- Sección "Avanzado: audio" en Configuración: control automático de
  ganancia (AGC, activado por defecto), supresión de ruido, cancelación de
  eco, y sliders de ganancia manual de micrófono/reproducción en dB (-24 a
  +24), aplicados vía `Core.setAgcEnabled`/`setMicGainDb`/`setPlaybackGainDb`
  (verificados con javap antes de usarlos). Pensado para el caso de un
  micrófono que se escucha bajo en comparación con otras apps del teléfono.
- Atribución a Agnov Solutions en la pantalla principal (enlace a
  agnov.cl), ya que AMELI Softphone es una plataforma de Agnov Solutions.

## [0.2.2]

### Cambiado

- Pulido visual general con la identidad de marca (navy/cian) en el resto
  de pantallas:
  - Dialer: teclado numérico con botones circulares (con letras ABC/DEF/…
    debajo de cada número, como un teléfono real) y botón de llamar
    circular destacado en el color primario, en vez de botones rectangulares.
  - Configuración: los campos se agrupan en tarjetas por sección ("Cuenta
    SIP", "NAT y conectividad", "Prioridad de códecs") en vez de un
    formulario plano con divisores.
  - Historial: cada llamada es una tarjeta con un punto de color indicando
    el resultado (verde=completada, rojo=perdida, amarillo=rechazada,
    gris=cancelada), y también usa el número/usuario en vez de la URI SIP
    cruda cuando no hay nombre de contacto.

## [0.2.1]

### Corregido

- Prioridad de códecs en Configuración: la lista se renderizaba siempre en
  el orden fijo del enum `AudioCodec.entries` en vez de `uiState.codecPriority`,
  así que reordenar con ▲▼ no tenía ningún efecto visible aunque el estado
  interno sí cambiaba. Ahora la lista se arma a partir de
  `uiState.codecPriority` (los desmarcados quedan al final); además ▲▼ se
  deshabilitan para códecs desmarcados.

### Cambiado

- Pantallas de llamada (activa y entrante): en vez de mostrar la URI SIP
  cruda (`sip:203@192.168.1.1:5060`) como título cuando no hay nombre de
  contacto, se muestra solo el número/usuario ("203"). Se agrega un avatar
  circular con la inicial. Las estadísticas técnicas (códec, pérdida,
  jitter, RTT, ICE) y la dirección SIP completa pasan a un panel "Detalles
  de llamada" colapsable (oculto por defecto), en vez de mostrarse siempre.

## [0.2.0]

### Añadido

- Espera + segunda llamada: `CallManager` soporta hasta dos llamadas
  simultáneas por Call-ID SIP estable (`Core.getCalls()`/pausa/reanuda vía
  `Call.pause()`/`Call.resume()`). Nuevo `secondaryCallState` en
  `CallController`, mostrado en `ActiveCallScreen` con acciones contextuales
  (contestar/rechazar si está timbrando, o intercambiar/colgar/unir si está
  en espera). Al colgar la llamada en primer plano, la otra se reanuda
  automáticamente si estaba en espera.
- Transferencia de llamada: ciega (`Call.transferTo(Address)`, no la versión
  deprecada `transfer(String)`) y consultiva (`Call.transferToAnother`,
  uniendo la llamada en espera con la de primer plano).
- Indicador de buzón de voz (MWI): `AccountListenerStub.onMessageWaitingIndicationChanged`
  actualiza un contador visible en la pantalla principal cuando el PBX
  reporta mensajes de voz nuevos.
- Volver a llamar desde el Historial con un tap (ícono de llamada por fila),
  reutilizando la misma verificación de permiso `RECORD_AUDIO` que el
  Dialer (extraída a `rememberCallPermissionLauncher`, reutilizable).
- Silenciar el timbre en la pantalla de llamada entrante sin colgar
  (`Core.stopRinging()`), sin afectar la posibilidad de contestar o rechazar
  después.
- Modo No Molestar: mientras está activo (switch en la pantalla principal),
  `CallManager` rechaza automáticamente las llamadas entrantes
  (`Call.decline(Reason.Busy)`) antes de timbrar o notificar; igual quedan
  registradas en el historial como rechazadas.
- Aviso de actualización: al abrir la app se consulta la última GitHub
  Release (`UpdateChecker`, vía `org.json` — sin dependencias nuevas) y, si
  hay una versión más nueva, se muestra un diálogo para descargarla
  directamente (abre el APK del asset o la página de la Release). Recuerda
  la versión rechazada para no insistir con la misma (`UpdateDismissalStore`).
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
