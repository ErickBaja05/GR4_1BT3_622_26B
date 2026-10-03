## CP-CU04-01 – Caso de prueba de Consultar evolución de calidad

**Entrada**

- El Estudiante (o Docente) solicita consultar la evolución de calidad de "Proyecto Demo".
- El sistema informa las auditorías registradas del proyecto: 05/10/2026 (25/35, 71,4 %) y 12/10/2026 (25/35, 71,4 %).
- El Estudiante (o Docente) indica las auditorías del 12/10/2026 y del 05/10/2026.
- El sistema toma como base la auditoría del 05/10/2026 (la más antigua) y como comparada la del 12/10/2026.
- El sistema obtiene el resultado de cada regla en ambas auditorías.

**Salida**

- Se calcula la variación: 0 puntos y 0 %.
- Se clasifican los hallazgos: R2 Nuevo (BAJA), R3 Corregido (MEDIA) y R4 Persistente (ALTA); R1 cumple en ambas y no genera hallazgo.
- Se muestran los resultados de ambas auditorías, la variación y los totales: 1 Nuevo, 1 Persistente y 1 Corregido.
- No se registra ni se modifica ninguna información.

**Condiciones**

- El usuario debe estar autenticado; el estudiante solo consulta su propio proyecto y el docente puede consultar cualquiera.
- El proyecto debe tener al menos dos auditorías registradas:
    - Auditoría del 05/10/2026: R1 y R2 cumplen, R3 y R4 no cumplen → 25/35, 71,4 % (es la auditoría de CP-CU03-01).
    - Auditoría del 12/10/2026: R1 y R3 cumplen, R2 y R4 no cumplen → 25/35, 71,4 %.
- Las dos auditorías indicadas deben ser distintas.
- La consulta no ejecuta una auditoría nueva.