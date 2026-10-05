CU04: Consultar evolución de calidad
•	Identificador: CP-CU04-01
•	Nombre: Consulta exitosa de la evolución de calidad entre dos auditorías registradas con igual puntaje y cambios en sus hallazgos
Precondiciones
•	El estudiante debe estar autenticado.
•	El proyecto debe tener al menos dos auditorías registradas.
•	Se debió completar el caso de prueba CP-CU03-01.
Datos de entrada
•	Nombre del proyecto: "Proyecto Demo".
•	Auditorías seleccionadas: A2 y A1.
Procedimiento
•	El estudiante solicita consultar la evolución de su proyecto "Proyecto Demo".
•	El sistema informa las auditorías registradas: A1 del 05/10/2026 (25 de 35, 71,4 %) y A2 del 12/10/2026 (25 de 35, 71,4 %).

Resultado Esperado
•	Se calcula la variación: 0 puntos y 0 % entre A1 y A2.
•	Se clasifica cada hallazgo: Archivo README es Nuevo (BAJA), Carpeta src es Corregido (MEDIA) y Sin archivos env es Persistente (ALTA); Archivo gitignore no tiene hallazgo.
•	Se muestra al estudiante la variación, la clasificación de cada hallazgo y los totales por estado: 1 Nuevo, 1 Persistente y 1 Corregido.
