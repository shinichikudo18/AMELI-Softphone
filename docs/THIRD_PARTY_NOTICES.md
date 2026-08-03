# Avisos de terceros

## Liblinphone SDK

- **Artefacto**: `org.linphone:linphone-sdk-android:5.5.13` (empaquetado
  `.aar`, incluye las librerías nativas `liblinphone.so`, `libmediastreamer2.so`,
  `libortp.so`, `libbctoolbox.so`, `libsrtp2.so`, entre otras).
- **Repositorio Maven oficial**: `https://download.linphone.org/maven_repository`
  (anunciado en
  [linphone.org/en/news/liblinphone-sdk-available-through-git-maven-repository](https://www.linphone.org/en/news/liblinphone-sdk-available-through-git-maven-repository/)).
- **Origen**: `https://gitlab.linphone.org/BC/public/linphone-sdk` (según el
  POM del artefacto).
- **Licencia declarada en el POM de la versión 5.5.13**: GNU General Public
  License, versión 3.0 (<https://www.gnu.org/licenses/gpl-3.0.en.html>).

La licencia de Liblinphone (GPLv3) y la de AMELI Softphone (AGPLv3) son
compatibles: AGPLv3 es una variante de GPLv3 con una condición adicional para
uso en red, por lo que un proyecto AGPLv3 puede enlazar con dependencias
GPLv3.

## Otras dependencias

Ninguna otra dependencia de terceros usada en este proyecto (AndroidX,
Kotlin, Jetpack Compose) impone obligaciones de atribución adicionales más
allá de sus propias licencias Apache 2.0, compatibles con AGPLv3.
