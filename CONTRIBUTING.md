# Contribuir a AMELI Softphone

Gracias por tu interés en contribuir. Este proyecto integra el SDK de
Liblinphone; por favor lee esta guía antes de enviar cambios.

## Flujo de trabajo

1. Haz un fork del repositorio y crea una rama descriptiva
   (`feature/registro-sip`, `fix/dtmf-tone`, etc.).
2. Verifica que el proyecto compila y pasa las pruebas antes de abrir un PR:

   ```bash
   ./gradlew assembleDebug testDebugUnitTest lintDebug
   ```

3. Abre un Pull Request describiendo el cambio, el motivo y cómo probarlo.
   El workflow de GitHub Actions ejecutará build, tests y lint automáticamente.

## Estilo de código

- Kotlin idiomático, siguiendo el estilo oficial (`kotlin.code.style=official`).
- Arquitectura MVVM: la lógica SIP/VoIP vive en clases dedicadas
  (`LinphoneManager`, `CallManager`, etc.), nunca directamente en Composables
  o Activities.
- Sin comentarios que expliquen "qué" hace el código cuando el nombre ya lo
  dice; comenta solo decisiones no evidentes.

## Seguridad

- Nunca incluyas credenciales SIP reales, tokens o contraseñas en commits,
  código o logs.
- No agregues archivos con datos sensibles (`local.properties`, keystores,
  `.env`, etc.) — ya están en `.gitignore`.
- Si encuentras una vulnerabilidad, sigue el proceso descrito en
  [SECURITY.md](SECURITY.md) en lugar de abrir un issue público.

## Liblinphone

Antes de usar una clase o método del SDK de Liblinphone, verifica que exista
en la versión exacta integrada en `gradle/libs.versions.toml`. Si una API
cambió entre versiones del SDK, documenta el cambio en el PR.

## Licencia

Al contribuir, aceptas que tu contribución se distribuya bajo los términos de
la [GNU Affero General Public License v3.0](LICENSE).
