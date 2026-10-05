CU03: Ejecutar auditoría de calidad del repositorio
Identificador: CP-CU03-01
Nombre: Ejecución exitosa de auditoría de calidad con cumplimiento parcial de las reglas activas
Precondiciones
•	El Estudiante debe estar autenticado y tener un proyecto llamado “Proyecto Demo” asociado a su perfil.
•	Deben existir estas 4 reglas activas (tipo de motor, parámetro, ponderación y severidad):
-	Archivo gitignore: PRESENCIA_ARCHIVO, `.gitignore`, 20, ALTA.
-	Archivo README: PRESENCIA_ARCHIVO, `README.md`, 5, BAJA.
-	Carpeta src: ESTRUCTURA_CARPETAS, `src`, 5, MEDIA.
-	Sin archivos env: RESTRICCION_ARCHIVOS, `.env`, 5, ALTA.
•	El repositorio debe ser público y accesible desde la API de GitHub.
Datos de entrada
•	Nombre del proyecto: "Sistema de Gestión Escolar".
•	Enlace de repositorio: "https://github.com/org/proyecto-escolar".
Procedimiento
•	El estudiante solicita auditar su proyecto "Proyecto Demo" (github.com/estudiante-demo/proyecto-demo).
•	El sistema informa las 4 reglas activas: Archivo gitignore (20, ALTA), Archivo README (5, BAJA), Carpeta src (5, MEDIA) y Sin archivos env (5, ALTA), con un puntaje máximo de 35.
•	El estudiante confirma la auditoría.
•	El sistema solicita a GitHub la estructura del repositorio.
•	GitHub entrega la estructura: .gitignore, README.md, .env y docs/.
