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

- Fase 1: proyecto base compilable — módulo `app` con Kotlin, Jetpack Compose
  (Material 3), tema propio, pantalla de bienvenida y estructura MVVM inicial.
- Configuración de Gradle (Kotlin DSL) con Gradle 9.5.0 y Android Gradle
  Plugin 9.3.1, `compileSdk`/`targetSdk` 36, `minSdk` 26.
- Documentación base del repositorio: README, LICENSE (AGPLv3), CONTRIBUTING,
  SECURITY, `.gitignore` y `local.properties.example`.

## [0.1.0] - Sin publicar

Versión inicial en desarrollo. Aún no cuenta con integración SIP funcional.
