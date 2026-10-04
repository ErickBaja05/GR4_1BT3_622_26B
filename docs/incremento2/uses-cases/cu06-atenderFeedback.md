**Caso de Uso:** `atenderRetroalimentacion` (CU06)

### 1. Datos Estandarizados del Caso de Uso

**Entradas:**

1. **`idRetroalimentacion`**: Número entero largo (`Long`). Debe ser no nulo, positivo (`> 0`) y corresponder a un registro existente en el sistema.
2. **`respuestaTecnica`**: Cadena de texto (`String`). Debe ser no nula, no vacía y no estar compuesta exclusivamente por espacios en blanco (`!respuestaTecnica.isBlank()`). Debe contener una longitud de entre 10 y 1000 caracteres, detallando la orientación técnica, recomendaciones de solución y buenas prácticas para subsanar el hallazgo.
3. **`estudianteId`**: Número entero largo (`Long`). Identificador del estudiante responsable del proyecto de software al cual se dirigirá la notificación técnica.

**Salidas:**

1. **`mensajeConfirmacion`**: Cadena de texto con el valor literal: `"Retroalimentación atendida exitosamente"`.
2. **`notificacion`**: Notificación técnica despachada al estudiante responsable del proyecto con el detalle de la retroalimentación.
3. **`estadoActualizado`**: Cambio de estado de la entidad `Retroalimentacion` a `"ATENDIDA"`.
4. **`fechaRespuesta`**: Registro de la marca temporal (`LocalDateTime.now()`) con la fecha y hora de atención formal.

**Actores:**
* **Docente** (Actor Primario): Quien revisa las consultas técnicas y elabora las pautas de corrección.
* **Estudiante** (Actor Secundario / Receptor): Quien recibe la notificación y orientación para mejorar su código.

**Precondición:**
* El Docente se encuentra autenticado en el sistema con sesión activa.
* Existe al menos una solicitud de retroalimentación registrada en estado `"PENDIENTE"` asociada a un hallazgo de auditoría previa.

**Postcondición:**
* La retroalimentación seleccionada actualiza su estado a `"ATENDIDA"` y persiste la respuesta técnica junto con su fecha/hora de atención en la base de datos.
* Se notifica al estudiante responsable.
* No se modifican los puntajes ni el resultado histórico de la auditoría evaluada.

---

### 2. Escenario Básico (Flujo Principal)

1. El caso de uso inicia cuando el Docente accede a la bandeja de retroalimentaciones pendientes.
2. El Sistema consulta y presenta la lista de solicitudes de retroalimentación que poseen estado `"PENDIENTE"`, detallando el proyecto, la fecha de auditoría, la regla incumplida y la severidad del hallazgo.
3. El Docente selecciona la solicitud de retroalimentación que desea atender.
4. El Sistema presenta el detalle completo del hallazgo (regla evaluada, evidencia técnica, severidad y la duda o justificación redactada previamente por el estudiante).
5. El Docente suministra el contenido de la `respuestaTecnica` con las recomendaciones correctivas y confirma el envío.
6. El Sistema extrae los parámetros de la solicitud: `idRetroalimentacion`, `respuestaTecnica` y `estudianteId`.
7. El Sistema valida que `idRetroalimentacion` sea no nulo y corresponda a un formato numérico válido.
8. El Sistema verifica en la base de datos la existencia de la entidad `Retroalimentacion` asociada al `idRetroalimentacion`.
9. El Sistema verifica que el estado de la `Retroalimentacion` sea estrictamente igual a `"PENDIENTE"`.
10. El Sistema valida que `respuestaTecnica` no sea nula, no esté vacía y contenga una longitud de entre 10 y 1000 caracteres.
11. El Sistema asigna el texto de `respuestaTecnica` a la entidad `Retroalimentacion`.
12. El Sistema actualiza el atributo `estado` de la `Retroalimentacion` a `"ATENDIDA"`.
13. El Sistema establece el atributo `fechaRespuesta` con la fecha y hora actual del sistema (`LocalDateTime.now()`).
14. El Sistema persiste la entidad `Retroalimentacion` actualizada en la base de datos.
15. El Sistema despacha una notificación al estudiante responsable (`estudianteId`) informando que su consulta sobre el hallazgo ha sido atendida.
16. El caso de uso finaliza cuando el Sistema emite la salida `mensajeConfirmacion`: `"Retroalimentación atendida exitosamente"` y redirige al Docente a la lista actualizada de pendientes.

---

### 3. Escenarios Alternativos

#### Flujo Alternativo A: Validación de Respuesta Técnica Vacía o Inválida (Error de Negocio)
* **Condición:** En el paso 10 del flujo básico, el Docente envía una `respuestaTecnica` vacía, compuesta únicamente por espacios en blanco o con una longitud menor a 10 caracteres.
* **Comportamiento del Sistema:**
  1. El Sistema detecta el incumplimiento del criterio de validación sintáctica/negocio (`respuestaTecnica.isBlank() || length < 10`).
  2. El Sistema interrumpe el flujo y cancela la operación antes de invocar la persistencia en el DAO.
  3. El Sistema genera una excepción de negocio (`ExcepcionNegocio`) con el mensaje: `"La respuesta técnica es obligatoria y debe contener al menos 10 caracteres"`.
  4. El Sistema retorna al formulario de atención, conservando la información del hallazgo y presentando un mensaje de alerta: `"Error: La respuesta técnica no puede estar vacía"`.
  5. El estado de la `Retroalimentacion` permanece inalterado en `"PENDIENTE"`.

#### Flujo Alternativo B: Solicitud Previamente Atendida o Inexistente (Conflicto de Estado)
* **Condición:** En el paso 8 o 9 del flujo básico, la solicitud no existe en la base de datos o su estado ya no es `"PENDIENTE"` (por ejemplo, atendida concurrentemente por otro docente/mentor).
* **Comportamiento del Sistema:**
  1. El Sistema verifica que la entidad no existe o que su estado actual es `"ATENDIDA"`.
  2. El Sistema rechaza la modificación para evitar sobreescritura inconsistente.
  3. El Sistema emite un mensaje informativo: `"La solicitud seleccionada ya ha sido atendida previamente o no se encuentra disponible"`.
  4. El Sistema redirige a la lista de solicitudes pendientes actualizada.

#### Flujo Alternativo C: Cancelación Voluntaria del Docente (Descarte)
* **Condición:** En el paso 5 del flujo básico, el Docente decide posponer la atención o cancelar la redacción de la respuesta.
* **Comportamiento del Sistema:**
  1. El Docente presiona la opción "Cancelar / Volver".
  2. El Sistema no ejecuta ninguna petición de modificación ni despacha parámetros al servicio backend.
  3. El Sistema descarta el texto temporal no enviado.
  4. La entidad `Retroalimentacion` permanece en la base de datos intacta con estado `"PENDIENTE"`.
  5. El Sistema retorna a la bandeja de solicitudes pendientes.
