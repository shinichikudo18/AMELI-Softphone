# Compilación

## Requisitos

- JDK 17 o superior (se ha validado con JDK 21).
- Android SDK con `platforms;android-36` y `build-tools;36.0.0` instalados.
- Conexión a internet la primera vez, para descargar Gradle 9.5.0 (wrapper),
  el Android Gradle Plugin y las dependencias de Maven/Google.

## Pasos

1. Copia el archivo de configuración local de ejemplo:

   ```bash
   cp local.properties.example local.properties
   ```

2. Edita `local.properties` y ajusta `sdk.dir` a la ruta de tu Android SDK.
3. Compila la app de depuración:

   ```bash
   ./gradlew assembleDebug
   ```

4. Ejecuta las pruebas unitarias y lint:

   ```bash
   ./gradlew testDebugUnitTest lintDebug
   ```

5. (Opcional) Ejecuta las pruebas instrumentadas en un emulador/dispositivo
   conectado:

   ```bash
   ./gradlew connectedDebugAndroidTest
   ```

## Notas sobre el entorno de build

- Gradle Wrapper: Gradle 9.5.0.
- Android Gradle Plugin: 9.3.1, que incluye soporte a Kotlin embebido — por
  eso `app/build.gradle.kts` no aplica el plugin `org.jetbrains.kotlin.android`
  por separado, solo `org.jetbrains.kotlin.plugin.compose` para el compilador
  de Compose.
- `compileSdk`/`targetSdk` se fijan en 36 según especificación del proyecto;
  algunas dependencias de AndroidX se han fijado a versiones compatibles con
  `compileSdk 36` (en vez de la última disponible, que ya requiere API 37).
- El historial de llamadas usa Room con KSP (no kapt) como procesador de
  anotaciones.

## Descargar un APK ya compilado

No hace falta compilar el proyecto para probarlo:

- **Cada push a `main`** genera un APK de depuración descargable desde la
  pestaña [Actions](../../actions) del repositorio, como artifact del
  workflow `Android CI` (`ameli-softphone-debug`).
- **Cada tag `vX.Y.Z`** genera además un APK de release adjunto a una
  [Release](../../releases) de GitHub, listo para descargar e instalar.

## Configurar la firma de release (opcional)

Sin configuración adicional, el APK de release se firma con la firma de
debug (útil para probar, no para publicar). Para firmarlo con una clave de
release propia, añade estos secretos en GitHub
(`Settings → Secrets and variables → Actions`):

| Secreto | Contenido |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Tu keystore (`.jks`/`.keystore`) codificado en base64: `base64 -w0 mi-release.keystore` |
| `RELEASE_KEYSTORE_PASSWORD` | Contraseña del keystore |
| `RELEASE_KEY_ALIAS` | Alias de la clave dentro del keystore |
| `RELEASE_KEY_PASSWORD` | Contraseña de la clave |

El workflow nunca imprime el contenido de estos secretos en los logs; solo
decodifica el keystore a un archivo temporal del runner.
