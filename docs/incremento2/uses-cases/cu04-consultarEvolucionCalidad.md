
**Caso de Uso:** `consultarEvolucionCalidad`

### 1. Datos Estandarizados del Caso de Uso

**Entradas:**

1. **`proyectoId`**: Número entero positivo estrictamente mayor a cero (`> 0`). Debe ser no nulo y corresponder a un `Proyecto` registrado en el sistema. Si el actor es Estudiante, el proyecto debe estar registrado a su nombre.

2. **`auditoriaIdA`**: Número entero positivo estrictamente mayor a cero (`> 0`). Debe ser no nulo y corresponder a una `Auditoria` registrada en el historial del proyecto indicado.

3. **`auditoriaIdB`**: Número entero positivo estrictamente mayor a cero (`> 0`). Debe ser no nulo, corresponder a una `Auditoria` registrada en el historial del proyecto indicado y ser distinto de `auditoriaIdA`.

**Salidas:**

1. **`historialAuditorias`**: Lista de las auditorías registradas del proyecto, ordenadas por fecha, cada una con su `auditoriaId`, `fechaHora`, `puntajeObtenido`, `puntajeMaximo` y `porcentaje`.

2. **`evolucionCalidad`**: Resultado de la comparación, compuesto por:
* Los datos de la auditoría base y de la auditoría comparada (`fechaHora`, `puntajeObtenido`, `puntajeMaximo`, `porcentaje`).
* `variacionPuntaje`: Número entero calculado como `puntajeObtenido(comparada) - puntajeObtenido(base)`. Puede ser positivo, negativo o cero.
* `variacionPorcentaje`: Número decimal calculado como `porcentaje(comparada) - porcentaje(base)`, redondeado a un decimal. Puede ser positivo, negativo o cero.
* La lista de comparaciones por regla, cada una con el `nombreRepresentativo` de la regla, el `nivelSeveridad` y su estado de evolución: `"Nuevo"`, `"Persistente"` o `"Corregido"`.
* Los totales de hallazgos por estado (`totalNuevos`, `totalPersistentes`, `totalCorregidos`).

3. **`mensajeError`**: Cadena de texto con uno de los valores literales definidos en los escenarios alternativos.

**Actor:** Estudiante, Docente

**Precondición:**
* El Estudiante o el Docente se encuentra autenticado en el sistema.
* El proyecto a consultar está registrado.
* El proyecto tiene al menos dos auditorías registradas.

**Postcondición:** El Sistema informa la evolución de calidad entre las dos auditorías indicadas. No se modifica ni se registra ningún dato: no se ejecuta una auditoría nueva, no se consulta GitHub y los estados `"Nuevo"`, `"Persistente"` y `"Corregido"` se calculan en la consulta sin guardarse.

---
ESCENARIO BÁSICO:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado del que desea consultar la evolución de calidad.
2. El Sistema verifica cuántas Auditorías tiene registradas el Proyecto.
3. SI el Proyecto tiene al menos dos Auditorías registradas ENTONCES el Sistema informa las Auditorías con su fecha y hora, puntajeObtenido, puntajeMaximo y porcentaje.
4. El Estudiante o el Docente indica dos Auditorías del historial del Proyecto.
5. El Sistema verifica que las dos Auditorías indicadas sean distintas.
6. SI las Auditorías son distintas ENTONCES el Sistema toma la Auditoría más antigua como base y la más reciente como comparada.
7. El Sistema obtiene el resultado de cada Regla en la Auditoría base y en la Auditoría comparada.
8. El Sistema calcula la variación del puntajeObtenido y del porcentaje entre la Auditoría base y la Auditoría comparada.
9. El Sistema compara el resultado de cada Regla: SI la Regla se cumplió en la base y NO se cumple en la comparada ENTONCES clasifica su Hallazgo como “Nuevo”; SI la Regla NO se cumplió en ninguna de las dos ENTONCES lo clasifica como “Persistente”; SI la Regla NO se cumplió en la base y se cumple en la comparada ENTONCES lo clasifica como “Corregido”.
10. El Sistema cuenta los Hallazgos clasificados como “Nuevo”, “Persistente” y “Corregido”.
11. El Sistema informa la variación del puntajeObtenido y del porcentaje, el estado de cada Hallazgo y los totales por estado.
12. El caso de uso finaliza con la consulta de la evolución de calidad del Proyecto, Sin modificar ninguna Auditoría.

---
ESCENARIO ALTERNATIVO 1:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado del que desea consultar la evolución de calidad.
2. El Sistema verifica cuántas Auditorías tiene registradas el Proyecto.
3. SI el Proyecto NO tiene al menos dos Auditorías registradas ENTONCES el Sistema emite el mensaje “El proyecto no tiene suficientes auditorías para comparar”.
4. El caso de uso finaliza Sin consultar la evolución de calidad.

---
ESCENARIO ALTERNATIVO 2:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado del que desea consultar la evolución de calidad.
2. El Sistema verifica cuántas Auditorías tiene registradas el Proyecto.
3. SI el Proyecto tiene al menos dos Auditorías registradas ENTONCES el Sistema informa las Auditorías con su fecha y hora, puntajeObtenido, puntajeMaximo y porcentaje.
4. SI el Estudiante o el Docente NO indica las dos Auditorías ENTONCES el Sistema emite el mensaje “Consulta cancelada”.
5. El caso de uso finaliza Sin consultar la evolución de calidad.

---
ESCENARIO ALTERNATIVO 3:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado del que desea consultar la evolución de calidad.
2. El Sistema verifica cuántas Auditorías tiene registradas el Proyecto.
3. SI el Proyecto tiene al menos dos Auditorías registradas ENTONCES el Sistema informa las Auditorías con su fecha y hora, puntajeObtenido, puntajeMaximo y porcentaje.
4. El Estudiante o el Docente indica dos Auditorías del historial del Proyecto.
5. El Sistema verifica que las dos Auditorías indicadas sean distintas.
6. SI las dos Auditorías indicadas son la misma ENTONCES el Sistema emite el mensaje “Debe indicar dos auditorías distintas”.
7. El caso de uso regresa al paso 4.

---
ESCENARIO ALTERNATIVO 4:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado del que desea consultar la evolución de calidad.
2. El Sistema verifica cuántas Auditorías tiene registradas el Proyecto.
3. SI el Proyecto tiene al menos dos Auditorías registradas ENTONCES el Sistema informa las Auditorías con su fecha y hora, puntajeObtenido, puntajeMaximo y porcentaje.
4. El Estudiante o el Docente indica dos Auditorías del historial del Proyecto.
5. El Sistema verifica que las dos Auditorías indicadas sean distintas.
6. SI las Auditorías son distintas ENTONCES el Sistema toma la Auditoría más antigua como base y la más reciente como comparada.
7. El Sistema obtiene el resultado de cada Regla en la Auditoría base y en la Auditoría comparada.
8. El Sistema calcula la variación del puntajeObtenido y del porcentaje entre la Auditoría base y la Auditoría comparada.
9. SI todas las Reglas se cumplen en ambas Auditorías ENTONCES el Sistema emite el mensaje “No existen hallazgos en las auditorías comparadas” e informa la variación del puntajeObtenido y del porcentaje.
10. El caso de uso finaliza con la consulta de la evolución de calidad del Proyecto, Sin Hallazgos que clasificar.
