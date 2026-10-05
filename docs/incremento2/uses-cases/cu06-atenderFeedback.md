
**Caso de Uso:** `atenderRetroalimentacion`

### 1. Datos Estandarizados del Caso de Uso

**Entradas:**

1. **`solicitudId`**: Número entero positivo estrictamente mayor a cero (`> 0`). Debe ser no nulo y corresponder a una `SolicitudRetroalimentacion` registrada en el sistema con estado `"PENDIENTE"`.

2. **`orientacionTecnica`**: Cadena de texto. Debe ser no nula, no vacía, no estar compuesta solo por espacios en blanco y contener entre 10 y 1000 caracteres, con las recomendaciones de solución y las buenas prácticas para subsanar el `Hallazgo`.

**Salidas:**

1. **`solicitudesPendientes`**: Lista de las `SolicitudRetroalimentacion` con estado `"PENDIENTE"`, cada una con el `Proyecto`, la `fechaHora` de la `Auditoria`, el `nombreRepresentativo` de la `Regla` incumplida y el `nivelSeveridad` del `Hallazgo`.

2. **`detalleHallazgo`**: Información de la `SolicitudRetroalimentacion` seleccionada, compuesta por:
* El `nombreRepresentativo` de la `Regla` incumplida.
* La `evidencia` y el `nivelSeveridad` del `Hallazgo`.
* La `justificacion` redactada por el Estudiante.

3. **`notificacion`**: Notificación dirigida al Estudiante con la `orientacionTecnica` registrada por el Docente.

4. **`mensajeConfirmacion`**: Cadena de texto con el valor literal: `"Retroalimentación atendida exitosamente"`.

5. **`mensajeError`**: Cadena de texto con uno de los valores literales definidos en los escenarios alternativos.

**Actor:** Docente, Estudiante

**Precondición:**
* El Docente se encuentra autenticado en el sistema.
* El Estudiante solicitó previamente una retroalimentación sobre un Hallazgo de una Auditoría registrada.

**Postcondición:** La `SolicitudRetroalimentacion` queda con la `orientacionTecnica` registrada y con estado `"ATENDIDA"`, y el Estudiante recibe la orientación técnica. No se modifican los puntajes ni los resultados de la Auditoría.

---
ESCENARIO BÁSICO:

1. El caso de uso inicia con el Docente consultando las SolicitudRetroalimentacion pendientes.
2. El Sistema verifica SI existen SolicitudRetroalimentacion con estado “PENDIENTE”.
3. SI existen SolicitudRetroalimentacion con estado “PENDIENTE” ENTONCES el Sistema muestra la lista de SolicitudRetroalimentacion pendientes con el Proyecto, la fechaHora de la Auditoría, el nombreRepresentativo de la Regla incumplida y el nivelSeveridad del Hallazgo.
4. El Docente selecciona la SolicitudRetroalimentacion que desea responder.
5. El Sistema muestra la información del Hallazgo: el nombreRepresentativo de la Regla incumplida, la evidencia, el nivelSeveridad y la justificacion del Estudiante.
6. El Docente ingresa la orientacionTecnica.
7. El Docente confirma el envío de la orientacionTecnica.
8. SI el Docente confirma el envío ENTONCES el Sistema valida la orientacionTecnica.
9. SI la orientacionTecnica NO está en blanco y tiene más de 9 caracteres y menos de 1001 ENTONCES el Sistema registra la orientacionTecnica en la SolicitudRetroalimentacion.
10. El Sistema cambia el estado de la SolicitudRetroalimentacion a “ATENDIDA”.
11. El Sistema notifica la orientacionTecnica al Estudiante.
12. El Sistema emite el mensaje “Retroalimentación atendida exitosamente”.
13. El Estudiante recibe la orientacionTecnica.
14. El caso de uso termina con la atención de la SolicitudRetroalimentacion.

---
ESCENARIO ALTERNATIVO 1:

1. El caso de uso inicia con el Docente consultando las SolicitudRetroalimentacion pendientes.
2. El Sistema verifica SI existen SolicitudRetroalimentacion con estado “PENDIENTE”.
3. SI NO existen SolicitudRetroalimentacion con estado “PENDIENTE” ENTONCES el Sistema emite el mensaje “No existen solicitudes de retroalimentación pendientes”.
4. El caso de uso termina sin atender ninguna SolicitudRetroalimentacion.

---
ESCENARIO ALTERNATIVO 2:

1. El caso de uso inicia con el Docente consultando las SolicitudRetroalimentacion pendientes.
2. El Sistema verifica SI existen SolicitudRetroalimentacion con estado “PENDIENTE”.
3. SI existen SolicitudRetroalimentacion con estado “PENDIENTE” ENTONCES el Sistema muestra la lista de SolicitudRetroalimentacion pendientes con el Proyecto, la fechaHora de la Auditoría, el nombreRepresentativo de la Regla incumplida y el nivelSeveridad del Hallazgo.
4. El Docente selecciona la SolicitudRetroalimentacion que desea responder.
5. El Sistema muestra la información del Hallazgo: el nombreRepresentativo de la Regla incumplida, la evidencia, el nivelSeveridad y la justificacion del Estudiante.
6. El Docente ingresa la orientacionTecnica.
7. El Docente confirma el envío de la orientacionTecnica.
8. SI el Docente NO confirma el envío ENTONCES el Sistema emite el mensaje “Atención cancelada”.
9. El caso de uso termina sin registrar la orientacionTecnica.

---
ESCENARIO ALTERNATIVO 3:

1. El caso de uso inicia con el Docente consultando las SolicitudRetroalimentacion pendientes.
2. El Sistema verifica SI existen SolicitudRetroalimentacion con estado “PENDIENTE”.
3. SI existen SolicitudRetroalimentacion con estado “PENDIENTE” ENTONCES el Sistema muestra la lista de SolicitudRetroalimentacion pendientes con el Proyecto, la fechaHora de la Auditoría, el nombreRepresentativo de la Regla incumplida y el nivelSeveridad del Hallazgo.
4. El Docente selecciona la SolicitudRetroalimentacion que desea responder.
5. El Sistema muestra la información del Hallazgo: el nombreRepresentativo de la Regla incumplida, la evidencia, el nivelSeveridad y la justificacion del Estudiante.
6. El Docente ingresa la orientacionTecnica.
7. El Docente confirma el envío de la orientacionTecnica.
8. SI el Docente confirma el envío ENTONCES el Sistema valida la orientacionTecnica.
9. SI la orientacionTecnica está en blanco o NO tiene más de 9 caracteres y menos de 1001 ENTONCES el Sistema emite el mensaje “Orientación en blanco o inválida”.
10. El caso de uso termina sin registrar la orientacionTecnica.
