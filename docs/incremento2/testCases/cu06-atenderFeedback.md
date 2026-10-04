## CP-CU06-01 – Caso de prueba de Atender retroalimentación de hallazgo

**Entrada**

- El Docente accede al módulo de retroalimentaciones pendientes.
- El sistema informa las solicitudes registradas con estado "Pendiente"; entre ellas, la solicitud sobre el hallazgo R4 (severidad ALTA) del "Proyecto Demo" (auditoría del 12/10/2026).
- El Docente selecciona la solicitud del hallazgo R4.
- El sistema muestra el detalle:
    - Regla incumplida: R4 - Detección de archivos sensibles (presencia indebida de `.env` en la raíz del repositorio).
    - Severidad: ALTA (impacto de 10 puntos en la auditoría).
    - Duda planteada por el Estudiante: *"El hallazgo indica que se detectó el archivo `.env` en el repositorio remoto. ¿Cómo debemos gestionar las variables de entorno sin comprometer la seguridad del proyecto ni perder la configuración en local?"*
- El Docente redacta la respuesta técnica con las recomendaciones:
    *"Se debe eliminar inmediatamente el archivo `.env` del repositorio remoto y agregarlo al `.gitignore`. Para permitir que otros desarrolladores configuren su entorno, cree un archivo plantilla llamado `.env.example` en la raíz con los nombres de las variables y valores vacíos, documentando su uso en el `README.md`."*
- El Docente confirma el envío de la respuesta técnica.

**Salida**

- Se registra la respuesta técnica del Docente y la fecha/hora de atención en la retroalimentación.
- Se actualiza el estado de la retroalimentación a "Atendida".
- Se notifica al Estudiante responsable del proyecto con la orientación técnica brindada.
- El sistema actualiza la lista de solicitudes pendientes, retirando la atendida y mostrando la confirmación: "Retroalimentación atendida exitosamente".

**Condiciones**

- El usuario debe estar autenticado con rol Docente.
- El proyecto "Proyecto Demo" debe contar con una auditoría registrada (auditoría del 12/10/2026 con 25/35 puntos y 71,4 %).
- Debe existir una retroalimentación en estado "Pendiente" previamente solicitada por el Estudiante sobre el hallazgo R4 de dicha auditoría.
- La atención registra la respuesta y cambia el estado a "Atendida"; no altera los puntajes ni el resultado histórico de la auditoría.
