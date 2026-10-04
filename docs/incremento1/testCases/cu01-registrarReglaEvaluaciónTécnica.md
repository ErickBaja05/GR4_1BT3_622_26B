### Caso de Prueba

* **Identificador:** `CP-CU01-01`
* **Caso de Uso Asociado:** `registrarReglaEvaluacionTecnica`
* **Nombre de la Prueba:** Registro exitoso de regla de evaluación técnica con datos válidos
* **Objetivo:** Verificar que el sistema permita el registro y la persistencia de una nueva regla técnica cuando todos los parámetros de entrada cumplen las restricciones de formato, catálogo y unicidad

#### Precondiciones
* El usuario con rol Docente se encuentra debidamente autenticado en el sistema.
* En el sistema no existe previamente ninguna regla registrada con el nombre "Existencia de archivo README.md"[cite: 5, 7].

#### Datos de Entrada
* `tipoMotor`: `"PRESENCIA_ARCHIVO"`
* `nombreRepresentativo`: `"Existencia de archivo README.md"`
* `descripcionDetallada`: `"Un repositorio de calidad debe contener en la raíz el archivo README.md para documentar el propósito del proyecto"`
* `parametroExacto`: `"README.md"`
* `nivelSeveridad`: `"MEDIA"`
* `ponderacion`: `15`

#### Procedimiento / Pasos de Ejecución
1. El Docente suministra el conjunto de datos de entrada especificado para la nueva regla técnica.
2. El Docente solicita el registro formal de la regla técnica en el sistema.

#### Resultado Esperado
* El sistema valida y verifica exitosamente la totalidad de los datos suministrados.
* El sistema crea y almacena la nueva entidad Regla con los atributos suministrados y con su estado activa asignado en `true`.
* El sistema emite la salida `mensajeConfirmacion`: `"Regla registrada exitosamente"`.

#### Postcondiciones
* La regla "Existencia de archivo README.md" queda registrada en el estado persistente del sistema, activa y disponible para ser evaluada en auditorías de repositorios (CU03).
