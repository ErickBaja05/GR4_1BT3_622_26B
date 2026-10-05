CU02: Registrar proyecto académico
Identificador: CP-CU02-01
Nombre: Registro Exitoso de un proyecto académico
Precondiciones
•	El usuario con rol Estudiante se encuentra debidamente autenticado en el sistema.
•	En el sistema no existe previamente ninguna regla registrada con el nombre "Sistema de Gestión Escolar".
•	No se permite registrar de manera simultánea otro proyecto académico asociado al mismo enlace de repositorio durante la ejecución del caso de prueba.
Datos de entrada
•	Nombre del proyecto: "Sistema de Gestión Escolar".
•	Enlace de repositorio: "https://github.com/org/proyecto-escolar".
Procedimiento
•	El Estudiante ingresa el nombre del proyecto: "Sistema de Gestión Escolar".
•	El Estudiante proporciona el enlace del repositorio de GitHub: "https://github.com/org/proyecto-escolar".
Resultado Esperado
•	Se comprueba el enlace, se confirma que el repositorio es público, se extrae la información (carpetas, archivos) y se guarda el proyecto con su información académica.
•	El Estudiante recibe la confirmación de que se registró exitosamente el proyecto.
