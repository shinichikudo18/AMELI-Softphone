# AMELI Softphone

Softphone SIP de audio para Android, construido desde cero en Kotlin + Jetpack
Compose, integrando el SDK oficial de [Liblinphone](https://www.linphone.org/)
mediante su repositorio Maven oficial. Este proyecto **no** es una copia ni una
modificación de la aplicación Linphone Android: es una aplicación nueva e
independiente que consume el SDK de Liblinphone como dependencia.

> **Estado**: en desarrollo activo, por fases. Ver [CHANGELOG.md](CHANGELOG.md)
> para el detalle de lo implementado en cada versión.

## Características (versión 0.1.0 — solo audio)

- [x] Fase 1 — Proyecto base compilable (Kotlin, Compose, MVVM).
- [x] Fase 2 — Integración de Liblinphone SDK.
- [x] Fase 3 — Registro de cuenta SIP con estado en tiempo real.
- [x] Fase 4 — Llamadas salientes con teclado numérico.
- [x] Fase 5 — Llamadas entrantes con notificación y pantalla dedicada.
- [x] Fase 6 — Controles de audio en llamada activa (mute, altavoz, DTMF).
- [x] Fase 7 — Servicio en segundo plano (`LinphoneService`).
- [x] Fase 8 — Historial de llamadas y almacenamiento seguro de credenciales.
- [ ] Fase 9 — CI/CD con GitHub Actions y documentación completa.

## Requisitos

- Android Studio (o CLI) con JDK 17+.
- Android SDK: `compileSdk 36`, `targetSdk 36`, `minSdk 26`.
- Gradle Wrapper incluido (Gradle 9.5.0 + Android Gradle Plugin 9.3.1).

## Compilación

```bash
git clone <url-del-repositorio>
cd ameli-softphone
cp local.properties.example local.properties
# Edita local.properties y ajusta sdk.dir a la ruta de tu Android SDK.
./gradlew assembleDebug
```

Ver [docs/BUILDING.md](docs/BUILDING.md) para más detalle.

## Configuración de una cuenta SIP

Ver [docs/SIP_SETUP.md](docs/SIP_SETUP.md).

## Arquitectura

MVVM con `StateFlow` para exponer el estado de registro SIP y de llamadas a la
capa de UI (Jetpack Compose). La lógica de SIP/VoIP está aislada de la UI en
componentes dedicados (`LinphoneManager`, `LinphoneService`, `SipAccountManager`,
`CallManager`, `AudioRouteManager`, `LinphoneCoreListener`) para poder
evolucionar o testear cada capa de forma independiente.

## Seguridad

Ver [SECURITY.md](SECURITY.md) para la política de reporte de vulnerabilidades
y las prácticas de seguridad aplicadas (sin contraseñas en texto plano, sin
credenciales reales en el repositorio, validación TLS estricta, etc.).

## Licencia

Este proyecto se distribue bajo los términos de la [GNU Affero General Public
License v3.0](LICENSE). Los avisos de terceros (incluyendo la licencia exacta
del SDK de Liblinphone integrado) se encuentran en
[docs/THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md).

## Autor

Franco Navarrete (Agnov)
