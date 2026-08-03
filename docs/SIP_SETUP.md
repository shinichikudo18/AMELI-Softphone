# Configuración de una cuenta SIP

> Esta guía describe la configuración de cuenta SIP planeada para AMELI
> Softphone. La pantalla de configuración se implementa en la Fase 3; hasta
> entonces este documento sirve como referencia de los campos soportados.

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
