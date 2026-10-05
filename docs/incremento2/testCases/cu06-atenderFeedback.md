CU06: Atender retroalimentación de hallazgo
•	Identificador: CP-CU06-01
•	Nombre: Atención exitosa de una retroalimentación solicitada por un Estudiante.
Precondiciones
•	El usuario debe estar autenticado con rol Docente.
•	Debe existir una SolicitudRetroalimentación en estado "PENDIENTE".
Datos de entrada
•	SolicitudRetroalimentación: hallazgo R4 (severidad ALTA) del "Proyecto Demo".
•	Respuesta técnica: "Se debe eliminar inmediatamente el archivo .env del repositorio remoto y agregarlo al .gitignore. Para permitir que otros desarrolladores configuren su entorno, cree un archivo plantilla llamado .env.example en la raíz con los nombres de las variables y valores vacíos, documentando su uso en el README.md."
Procedimiento
•	El Docente accede al registro de retroalimentaciones pendientes.
•	Se presentan las solicitudes registradas con estado "Pendiente"; entre ellas, la solicitud sobre el hallazgo R4 (severidad ALTA) del "Proyecto Demo" (auditoría del 12/10/2026).
•	El Docente selecciona la solicitud del hallazgo R4 y examina la duda del Estudiante.
•	El Docente formula y redacta la respuesta técnica con las recomendaciones.
•	El Docente confirma la remisión de la respuesta técnica.
Resultado Esperado
•	Se registra la respuesta técnica del Docente en la retroalimentación.
•	Se actualiza el estado de la retroalimentación a "ATENDIDA".
•	Se notifica al Estudiante responsable del proyecto con la orientación técnica brindada.
