
**Caso de Uso:** `ejecutarAuditoriaCalidadRepositorio`

### 1. Datos Estandarizados del Caso de Uso

**Entradas:**

1. **`proyectoId`**: Número entero positivo estrictamente mayor a cero (`> 0`). Debe ser no nulo y corresponder a un `Proyecto` registrado en el sistema. Si el actor es Estudiante, el proyecto debe estar registrado a su nombre.
2. **`confirmacion`**: Valor booleano. Debe ser no nulo y tener el valor `true` para continuar con la auditoría. Si es `false`, la auditoría se cancela.

**Salidas:**

1. **`resumenAuditoria`**: Información previa a la ejecución, compuesta por:
* Los datos del proyecto (`nombre`, `urlRepositorio`).
* La lista de reglas activas, cada una con su `nombreRepresentativo`, `tipoMotor`, `parametroExacto`, `nivelSeveridad` y `ponderacion`.
* `puntajeMaximo`: Número entero igual a la suma de la `ponderacion` de todas las reglas activas.

2. **`resultadoAuditoria`**: Resultado final de la auditoría, compuesto por:
* `fechaHora` de ejecución.
* `puntajeObtenido`: Número entero igual a la suma de los puntos obtenidos en cada regla.
* `puntajeMaximo`: Número entero igual a la suma de la `ponderacion` de todas las reglas evaluadas.
* `porcentaje`: Número decimal calculado como `(puntajeObtenido / puntajeMaximo) * 100`, redondeado a un decimal.
* La lista de resultados por regla (`nombreRepresentativo`, `cumple`, `puntosObtenidos`).
* La lista de hallazgos, cada uno con su `nivelSeveridad`, `evidencia` y `recomendacion`.

3. **`mensajeError`**: Cadena de texto con uno de los valores literales definidos en los escenarios alternativos.

**Actor:** Estudiante, Docente

**Actor secundario:** GitHub (Sistema Externo)

**Precondición:** 
* El Estudiante o el Docente se encuentra autenticado en el sistema
* El proyecto a auditar está registrado.
* Existe almenos una regla de evaluación activa. 

**Postcondición:** Una nueva `Auditoria` queda registrada en el historial del proyecto junto con un `ResultadoRegla` por cada regla activa evaluada y un `Hallazgo` por cada regla incumplida. La auditoría registrada no puede modificarse.

---
ESCENARIO BÁSICO:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado que desea auditar.
2. El Sistema verifica si existen Reglas con estado activa.
3. SI existe al menos una Regla activa ENTONCES el Sistema calcula el puntajeMaximo como la suma de la ponderacion de las Reglas activas.
4. El Sistema informa las Reglas activas con su nombreRepresentativo, ponderacion y nivelSeveridad, informa el puntajeMaximo y solicita la confirmación de la auditoría.
5. SI el Estudiante o el Docente confirma la auditoría ENTONCES el Sistema solicita a GitHub la estructura del repositorio que se encuentra en la urlRepositorio del Proyecto.
6. SI el repositorio es accesible ENTONCES GitHub entrega la estructura del repositorio con los nombres y rutas de sus archivos y carpetas.
7. El Sistema evalúa cada Regla activa sobre la estructura del repositorio según su tipoMotor y su parametroExacto.
8. SI la Regla se cumple ENTONCES el Sistema le asigna su ponderacion como puntos obtenidos; SI la Regla NO se cumple ENTONCES el Sistema le asigna cero puntos y genera un Hallazgo con el nivelSeveridad de la Regla, la evidencia del incumplimiento y la recomendación.
9. El Sistema calcula el puntajeObtenido como la suma de los puntos obtenidos y el porcentaje como el puntajeObtenido dividido para el puntajeMaximo, multiplicado por 100.
10. El Sistema registra la Auditoría con la fecha y hora, quien la solicitó, el puntajeObtenido, el puntajeMaximo, el porcentaje, el resultado de cada Regla y los Hallazgos generados.
11. El Sistema informa el puntajeObtenido, el puntajeMaximo, el porcentaje, el resultado de cada Regla y los Hallazgos.
12. El caso de uso finaliza con la Auditoría registrada.
---
ESCENARIO ALTERNATIVO 1:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado que desea auditar.
2. El Sistema verifica si existen Reglas con estado activa.
3. SI NO existe ninguna Regla activa ENTONCES el Sistema emite el mensaje “No existen reglas de evaluación activas para auditar el proyecto”.
4. El caso de uso finaliza Sin auditar el Proyecto.

---
ESCENARIO ALTERNATIVO 2:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado que desea auditar.
2. El Sistema verifica si existen Reglas con estado activa.
3. SI existe al menos una Regla activa ENTONCES el Sistema calcula el puntajeMaximo como la suma de la ponderacion de las Reglas activas.
4. El Sistema informa las Reglas activas con su nombreRepresentativo, ponderacion y nivelSeveridad, informa el puntajeMaximo y solicita la confirmación de la auditoría.
5. SI el Estudiante o el Docente NO confirma la auditoría ENTONCES el Sistema emite el mensaje “Auditoría cancelada”.
6. El caso de uso finaliza Sin auditar el Proyecto.

---
ESCENARIO ALTERNATIVO 3:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado que desea auditar.
2. El Sistema verifica si existen Reglas con estado activa.
3. SI existe al menos una Regla activa ENTONCES el Sistema calcula el puntajeMaximo como la suma de la ponderacion de las Reglas activas.
4. El Sistema informa las Reglas activas con su nombreRepresentativo, ponderacion y nivelSeveridad, informa el puntajeMaximo y solicita la confirmación de la auditoría.
5. SI el Estudiante o el Docente confirma la auditoría ENTONCES el Sistema solicita a GitHub la estructura del repositorio que se encuentra en la urlRepositorio del Proyecto.
6. SI el repositorio NO es accesible ENTONCES el Sistema emite el mensaje “No fue posible auditar el proyecto: el repositorio no existe o no es público”.
7. El caso de uso finaliza Sin registrar la Auditoría.

---
ESCENARIO ALTERNATIVO 4:

1. El caso de uso inicia con el Estudiante o el Docente indicando el Proyecto registrado que desea auditar.
2. El Sistema verifica si existen Reglas con estado activa.
3. SI existe al menos una Regla activa ENTONCES el Sistema calcula el puntajeMaximo como la suma de la ponderacion de las Reglas activas.
4. El Sistema informa las Reglas activas con su nombreRepresentativo, ponderacion y nivelSeveridad, informa el puntajeMaximo y solicita la confirmación de la auditoría.
5. SI el Estudiante o el Docente confirma la auditoría ENTONCES el Sistema solicita a GitHub la estructura del repositorio que se encuentra en la urlRepositorio del Proyecto.
6. SI el repositorio es accesible ENTONCES GitHub entrega la estructura del repositorio con los nombres y rutas de sus archivos y carpetas.
7. El Sistema evalúa cada Regla activa sobre la estructura del repositorio según su tipoMotor y su parametroExacto.
8. SI todas las Reglas se cumplen ENTONCES el Sistema asigna a cada Regla su ponderacion como puntos obtenidos.
9. El Sistema calcula el puntajeObtenido, igual al puntajeMaximo, y el porcentaje, igual a 100.
10. El Sistema registra la Auditoría con la fecha y hora, quien la solicitó, el puntajeObtenido, el puntajeMaximo, el porcentaje y el resultado de cada Regla, sin Hallazgos.
11. El Sistema emite el mensaje “El proyecto cumple todas las reglas; no se encontraron hallazgos” e informa el puntajeObtenido, el puntajeMaximo y el porcentaje.
12. El caso de uso finaliza con la Auditoría registrada sin Hallazgos.
