# Política de Seguridad

## Versiones soportadas

Mientras el proyecto esté en desarrollo activo (versión 0.x), solo la última
versión publicada recibe correcciones de seguridad.

## Reportar una vulnerabilidad

Si encuentras una vulnerabilidad de seguridad, **no abras un issue público**.
Repórtala de forma privada a través de la sección "Security" del repositorio
de GitHub (Security Advisories) o contactando directamente al autor del
proyecto. Incluye:

- Descripción de la vulnerabilidad y su impacto potencial.
- Pasos para reproducirla.
- Versión afectada.

Intentaremos responder en un plazo razonable y coordinar la publicación de un
parche antes de divulgar los detalles.

## Prácticas de seguridad aplicadas

- Las credenciales SIP (usuario/contraseña) no se almacenan en texto plano:
  se cifran con una clave AES-GCM generada y almacenada en Android Keystore
  (`SecureCredentialStore`), no con `androidx.security.crypto`, que está
  deprecado desde su versión 1.1.0.
- No se registran contraseñas, tokens ni cabeceras de autenticación en logs.
- La validación de certificados TLS nunca se desactiva; no se aceptan
  certificados no confiables por defecto.
- No existen credenciales SIP reales en el código fuente ni en el historial
  de commits. `local.properties.example` documenta la configuración esperada
  sin secretos.
- Los archivos sensibles (`local.properties`, keystores, `.env`) están
  excluidos vía `.gitignore`.
- La firma de release en CI se lee desde GitHub Secrets, nunca desde archivos
  versionados ni logs.
