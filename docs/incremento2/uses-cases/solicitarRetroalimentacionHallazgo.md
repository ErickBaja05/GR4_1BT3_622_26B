**Caso de Uso:** `solicitarRetroalimentacionHallazgo`

### 1. Datos Estandarizados del Caso de Uso

**Entradas:**

1. **`reporteId`**: Número entero positivo estrictamente mayor a cero (`> 0`). Debe ser no nulo y corresponder a un `ReporteAuditoria` registrado en el sistema. Si el actor es Estudiante, el proyecto asociado al reporte debe estar registrado a su nombre.

2. **`hallazgoId`**: Número entero positivo strictly mayor a cero (`> 0`). Debe ser no nulo, corresponder a un `Hallazgo` registrado dentro del reporte de auditoría indicado y pertenecer al listado de hallazgos del proyecto.

3. **`justificacionTecnica`**: Cadena de texto no vacía, con una longitud mínima de 10 caracteres y máxima de 1000 caracteres. Contiene el sustento técnico o la argumentación elaborada por el Estudiante para solicitar la revisión u orientación del hallazgo.

**Salidas:**

1. **`solicitudRetroalimentacion`**: Registro de la solicitud creada en el sistema, compuesto por: `solicitudId`, `hallazgoId`, `estudianteId`, `justificacionTecnica`, `fechaHoraCreacion` y el estado inicial `"Pendiente"`.

2. **`notificacionDocente`**: Registro o aviso emitido hacia el Docente asignado al proyecto/materia, con el `solicitudId`, `codigoProyecto`, `nombreEstudiante` y `fechaHora`.

3. **`mensajeInformativo`**: Cadena de texto con uno de los valores literales definidos en los escenarios alternativos.

**Actor:** Estudiante, Docente

**Precondición:**
* El Estudiante se encuentra autenticado en el sistema.
* El proyecto del Estudiante tiene al menos un reporte de auditoría generado con hallazgos registrados.

**Postcondición:** El Sistema registra una nueva solicitud de retroalimentación en estado `"Pendiente"` asociada al hallazgo e informa al Docente. No se modifican las reglas, los resultados de la auditoría ni el puntaje del proyecto.

---
ESCENARIO BÁSICO:

1. El caso de uso inicia con el Estudiante indicando el reporte de auditoría que desea consultar.
2. El Sistema obtiene y muestra los hallazgos registrados en el reporte de auditoría indicado.
3. El Estudiante indica el hallazgo observado sobre el cual requiere retroalimentación.
4. El Estudiante redacta la justificación técnica explicando la razón de su observación o duda sobre el hallazgo.
5. El Estudiante solicita la retroalimentación del hallazgo expresando su confirmación.
6. El Sistema verifica el estado de revisión del hallazgo indicado consultando si ya existe una solicitud de retroalimentación registrada para dicho hallazgo.
7. SI el hallazgo NO tiene una solicitud previa registrada ENTONCES el Sistema crea la nueva solicitud de retroalimentación asociando el `hallazgoId`, la `justificacionTecnica` del Estudiante, la fecha y hora actual y asignándole el estado `"Pendiente"`.
8. El Sistema notifica la creación de la nueva solicitud al Docente asignado al proyecto.
9. El Sistema emite el mensaje "Solicitud de retroalimentación enviada con éxito".
10. El caso de uso finaliza con el registro y notificación exitosa de la solicitud de retroalimentación.

---
ESCENARIO ALTERNATIVO 1:

1. El caso de uso inicia con el Estudiante indicando el reporte de auditoría que desea consultar.
2. El Sistema obtiene y muestra los hallazgos registrados en el reporte de auditoría indicado.
3. El Estudiante indica el hallazgo observado sobre el cual requiere retroalimentación.
4. El Estudiante redacta la justificación técnica explicando la razón de su observación.
5. El Estudiante solicita la retroalimentación del hallazgo.
6. El Sistema verifica el estado de revisión del hallazgo indicado.
7. SI el hallazgo ya cuenta con una solicitud registrada previamente ENTONCES el Sistema emite el mensaje "El hallazgo ya se encuentra en proceso de revisión".
8. El caso de uso finaliza Sin crear una nueva solicitud de retroalimentación.

---
ESCENARIO ALTERNATIVO 2:

1. El caso de uso inicia con el Estudiante indicando el reporte de auditoría que desea consultar.
2. El Sistema obtiene y muestra los hallazgos registrados en el reporte de auditoría indicado.
3. El Estudiante indica el hallazgo observado sobre el cual requiere retroalimentación.
4. El Estudiante redacta la justificación técnica.
5. SI la justificación técnica ingresada no cumple con la longitud requerida (está vacía o tiene menos de 10 caracteres) ENTONCES el Sistema emite el mensaje "Debe proporcionar una justificación técnica válida".
6. El caso de uso regresa al paso 4.

---
ESCENARIO ALTERNATIVO 3:

1. El caso de uso inicia con el Estudiante indicando el reporte de auditoría que desea consultar.
2. El Sistema obtiene y muestra los hallazgos registrados en el reporte de auditoría indicado.
3. SI el Estudiante no selecciona ningún hallazgo o decide cancelar el envío de la solicitud ENTONCES el Sistema emite el mensaje "Solicitud cancelada".
4. El caso de uso finaliza Sin registrar la solicitud de retroalimentación.