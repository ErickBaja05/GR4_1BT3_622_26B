## CP-CU03-01 – Caso de prueba de Ejecutar auditoría de calidad

**Entrada**

- El Estudiante (o Docente) solicita auditar "Proyecto Demo", registrado con su URL de GitHub.
- El sistema informa las 4 reglas activas y el puntaje máximo de 35.
- El Estudiante (o Docente) confirma la auditoría.
- El sistema obtiene desde GitHub la estructura del repositorio: `.gitignore`, `README.md`, `.env` y `docs/`.

**Salida**

- Se evalúa cada regla activa sobre la estructura: R1 y R2 cumplen; R3 y R4 no cumplen.
- Se calcula el resultado: 25 de 35, 71,4 %.
- Se registra la auditoría, el resultado de las 4 reglas y 2 hallazgos: R3 (severidad MEDIA) y R4 (severidad ALTA).
- Se muestra el puntaje obtenido, el porcentaje y los hallazgos.

**Condiciones**

- El usuario debe estar autenticado; el estudiante solo audita su propio proyecto y el docente puede auditar cualquiera.
- El proyecto debe estar registrado con la URL de su repositorio.
- Debe existir al menos una regla activa.
- El repositorio debe ser accesible desde GitHub; su estructura es: `.gitignore`, `README.md`, `.env`, `docs/`.