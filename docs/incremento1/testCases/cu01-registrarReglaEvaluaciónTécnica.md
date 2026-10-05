CU01: Registrar Regla de evaluación técnica
Identificador: CP-CU01-01
Nombre: Registro exitoso de regla de evaluación técnica con datos válidos
Precondiciones
•	El usuario con rol Docente se encuentra debidamente autenticado en el sistema.
•	En el sistema no existe previamente ninguna regla registrada con el nombre "Existencia de archivo README.md".
Datos entrada:
•	tipoMotor: "PRESENCIA_ARCHIVO"
•	nombreRepresentativo: "Existencia de archivo README.md"
•	descripcionDetallada: `"Un repositorio de calidad debe contener en la raíz el archivo README.md para documentar el propósito del proyecto"
•	parametroExacto: "README.md"
•	nivelSeveridad: "MEDIA"
•	ponderacion: 15
Procedimiento:
•	El Docente suministra el conjunto de datos de entrada especificado para la nueva regla técnica.
•	El Docente solicita el registro formal de la regla técnica.

 Resultado Esperado
•	El sistema valida y verifica exitosamente la totalidad de los datos suministrados.
•	El sistema crea y almacena la nueva entidad Regla con los atributos suministrados y con su estado activa asignado en `true`.
•	El sistema emite la salida `mensajeConfirmacion`: `"Regla registrada exitosamente"`.
•	 La regla "Existencia de archivo README.md" queda registrada en el sistema, activa y disponible para su utilización en auditorias. (CU03).
