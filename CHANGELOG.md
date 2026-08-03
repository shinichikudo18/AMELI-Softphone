# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).
Este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Añadido

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

- Fase 1: proyecto base compilable — módulo `app` con Kotlin, Jetpack Compose
  (Material 3), tema propio, pantalla de bienvenida y estructura MVVM inicial.
- Configuración de Gradle (Kotlin DSL) con Gradle 9.5.0 y Android Gradle
  Plugin 9.3.1, `compileSdk`/`targetSdk` 36, `minSdk` 26.
- Documentación base del repositorio: README, LICENSE (AGPLv3), CONTRIBUTING,
  SECURITY, `.gitignore` y `local.properties.example`.

## [0.1.0] - Sin publicar

Versión inicial en desarrollo. Aún no cuenta con integración SIP funcional.
