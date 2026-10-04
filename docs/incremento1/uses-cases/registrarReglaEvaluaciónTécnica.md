
**Caso de Uso:** `registrarReglaEvaluacionTecnica`

### 1. Datos Estandarizados del Caso de Uso

**Entradas:**

1. **`tipoMotor`**: Cadena de texto. Debe ser no nula, no vacía y corresponder exclusivamente a uno de los siguientes valores de catálogo: `"PRESENCIA_ARCHIVO"`, `"RESTRICCION_ARCHIVOS"`, `"ESTRUCTURA_CARPETAS"`.


2. **`nombreRepresentativo`**: Cadena de texto. Debe ser no nula, no vacía y contener entre 1 y 100 caracteres alfanuméricos o espacios.

3. **`descripcionDetallada`**: Cadena de texto. Debe ser no nula, no vacía y contener entre 1 y 200 caracteres.

4. * **`parametroExacto`**: Cadena de texto no nula, no vacía, sin espacios en blanco al inicio o al final, con una longitud de entre 1 y 100 caracteres. No puede contener secuencias de escape de directorio (`..`), ni barras dobles (`//`), ni caracteres no permitidos (`:`, `\`, `"`, `<`, `>`, `|`, `?`). Adicionalmente, debe cumplir la regla exclusiva según el `tipoMotor`:
* Si `tipoMotor == "PRESENCIA_ARCHIVO"`: Debe ser una ruta relativa de archivo. No debe empezar ni terminar con `/`, no debe contener el carácter comodín `*`, y debe incluir un nombre de archivo después del último separador `/` (ejemplos válidos: `README.md`, `docs/especificacion.pdf`, `.gitignore`).
* Si `tipoMotor == "ESTRUCTURA_CARPETAS"`: Debe ser una ruta relativa de directorio. No debe empezar ni terminar con `/`, no debe contener el carácter comodín `*`, y debe estar compuesta por nombres de carpeta válidos separados por `/` (ejemplos válidos: `src/test`, `docs`, `src/main/resources`).

* Si `tipoMotor == "RESTRICCION_ARCHIVOS"`: Debe ser una extensión prohibida (iniciando con `.` o `*.`, ej: `.class`, `*.jar`) o el nombre exacto de un archivo prohibido (ej: `.env`). No debe contener barras (`/` ni `\`).o.


5. **`nivelSeveridad`**: Cadena de texto. Debe ser no nula, no vacía y corresponder exclusivamente a uno de los siguientes valores de catálogo: `"ALTA"`, `"MEDIA"`, `"BAJA"`.


6. **`ponderacion`**: Número entero positivo strictly mayor a cero (`> 0`).

**Salidas:**

mensajeConfirmacion: Cadena de texto con el valor literal: "Regla registrada exitosamente"

**Actor:** Docente
**Precondición:** El Docente se encuentra autenticado en el sistema.
**Postcondición:** Una nueva regla queda registrada en el sistema con estado `activa = true`.
---

### 2. Escenario Básico



1. El caso de uso inicia cuando el Docente suministra los datos de la regla: `tipoMotor`, `nombreRepresentativo`, `descripcionDetallada`, `parametroExacto`, `nivelSeveridad` y `ponderacion`.
2. El Sistema valida que ninguno de los datos suministrados sea nulo ni esté vacío.
3. El Sistema valida que `tipoMotor` sea exactamente igual a uno de los tres valores permitidos: `"PRESENCIA_ARCHIVO"`, `"RESTRICCION_ARCHIVOS"` o `"ESTRUCTURA_CARPETAS"`.
4. El Sistema valida que `nombreRepresentativo` contenga una longitud de entre 1 y 100 caracteres.
5. El Sistema **verifica** que no exista en el sistema otra regla cuyo `nombreRepresentativo` coincida con el ingresado.
6. El Sistema valida que `descripcionDetallada` contenga una longitud de entre 1 y 200 caracteres.
7. El Sistema valida la sintaxis técnica de `parametroExacto`:
* 7.1. Valida que `parametroExacto` contenga una longitud de entre 1 y 100 caracteres.
* 7.2. Valida que `parametroExacto` no contenga secuencias de navegación `..` ni caracteres prohibidos (`:`, `\`, `"`, `<`, `>`, `|`, `?`).
* 7.3. Si `tipoMotor` es `"PRESENCIA_ARCHIVO"`: valida que `parametroExacto` no inicie ni termine con `/`, y no contenga `*`.
* 7.4. Si `tipoMotor` es `"ESTRUCTURA_CARPETAS"`: valida que `parametroExacto` no inicie ni termine con `/`, y no contenga `*`.
* 7.5. Si `tipoMotor` es `"RESTRICCION_ARCHIVOS"`: valida que `parametroExacto` no contenga barras `/` y que su formato corresponda a una extensión (inicie con `.` o `*.`) o al nombre de un archivo prohibido exacto (ej. `.env`).
8. El Sistema valida que `nivelSeveridad` sea exactamente igual a `"ALTA"`, `"MEDIA"` o `"BAJA"`.
9. El Sistema valida que `ponderacion` sea un número entero mayor a cero (`ponderacion > 0`).
10. El Sistema registra la nueva entidad `Regla` con los atributos suministrados y establece su atributo `activa` en `true`.
11. El caso de uso finaliza cuando el Sistema emite la salida `mensajeConfirmacion`: `"Regla registrada exitosamente"`.

---


