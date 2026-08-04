# Configuración de una cuenta SIP

Desde la pantalla principal, toca "Configurar cuenta SIP" para acceder al
formulario de configuración. Al guardar, la app aplica la configuración
inmediatamente contra Liblinphone e intenta registrarse.

## Campos de configuración

| Campo | Descripción |
|---|---|
| Nombre de usuario | Usuario SIP (`sip:usuario@dominio`). |
| Contraseña | Contraseña de autenticación SIP. Se almacena cifrada, nunca en texto plano. |
| Dominio / servidor SIP | Host o IP del servidor SIP/registrar. |
| Puerto | Puerto del servidor SIP (por defecto 5060 para UDP/TCP, 5061 para TLS). |
| Transporte | UDP, TCP o TLS. |
| Nombre para mostrar | Nombre visible en llamadas salientes. |
| SRTP | Activa cifrado de medios (SRTP) para las llamadas. |
| STUN | Servidor STUN opcional para resolución NAT. |
| ICE | Negociación de mejor ruta de medios (directa/STUN/TURN); recomendado si hay NAT de por medio. |
| TURN | Servidor/usuario/contraseña TURN, para redes con NAT simétrico donde ICE por sí solo no basta. La contraseña se guarda cifrada igual que la SIP. |
| Prioridad de códecs | Qué códecs de audio usar y en qué orden (Opus, PCMA, PCMU, G722). Los desmarcados quedan deshabilitados. |
| Avanzado: audio | Control automático de ganancia (AGC), supresión de ruido, cancelación de eco, y ganancia manual de micrófono/reproducción en dB (-24 a +24). Si el micrófono se escucha bajo, activa AGC o sube la ganancia manual. |

## Recomendaciones de seguridad

- Usa siempre TLS + SRTP cuando el servidor SIP lo soporte.
- No compartas tu `local.properties` ni ningún archivo con credenciales SIP
  reales — no deben subirse al repositorio.
- Si tu proveedor SIP requiere un STUN/TURN específico, verifica su
  documentación antes de configurarlo.

## Estados de registro

La aplicación mostrará el estado de registro SIP en tiempo real, mapeando los
estados internos de Liblinphone (`RegistrationState`) a mensajes
comprensibles: registrado, registrando, desconectado, error de autenticación,
servidor no disponible y error de certificado.

Si el registro falla por servidor no disponible o un error desconocido, la
app reintenta automáticamente con backoff exponencial (2s, 4s, 8s... hasta un
máximo de 60s). No reintenta automáticamente ante errores de autenticación o
de certificado, ya que esos requieren corregir la configuración, no reintentar.

## Estadísticas de la llamada

Durante una llamada activa, la app muestra en tiempo real el códec en uso,
pérdida de paquetes, jitter, RTT y el estado de la conexión ICE (directa, vía
STUN o vía TURN/relay).
