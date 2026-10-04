## Caso de Prueba: Consulta exitosa de la evolución de calidad entre dos auditorías registradas con igual puntaje y cambios en sus hallazgos

* **Identificador:** `CP-CU04-01`
* **Caso de Uso Asociado:** `consultarEvolucionCalidad`
* **Nombre de la Prueba:** Consulta exitosa de la evolución de calidad entre dos auditorías registradas con igual puntaje y cambios en sus hallazgos
* **Objetivo:** Verificar que el sistema compare dos auditorías registradas tomando la más antigua como base sin importar el orden en que el actor las indique, calcule la variación de puntaje y porcentaje y clasifique cada hallazgo como Nuevo, Persistente o Corregido sin modificar ni registrar datos
  
**Entrada**

- El estudiante solicita consultar la evolución de su proyecto "Proyecto Demo".
- El sistema informa las auditorías registradas: A1 del 05/10/2026 (25 de 35, 71,4 %) y A2 del 12/10/2026 (25 de 35, 71,4 %).
- El estudiante indica las dos auditorías a comparar, primero A2 y luego A1.
- El sistema toma A1 como base por ser la más antigua y A2 como comparada.
- El sistema obtiene el resultado de cada regla en ambas auditorías.
  
**Salida**

- Se calcula la variación: 0 puntos y 0 % entre A1 y A2.
- Se clasifica cada hallazgo: Archivo README es Nuevo (BAJA), Carpeta src es Corregido (MEDIA) y Sin archivos env es Persistente (ALTA); Archivo gitignore no tiene hallazgo.
- Se muestra al estudiante la variación, la clasificación de cada hallazgo y los totales por estado: 1 Nuevo, 1 Persistente y 1 Corregido.
- No se modifica ni se registra ningún dato.
  
**Condiciones**

- El estudiante debe estar autenticado y el proyecto debe estar registrado a su nombre.
- El proyecto debe tener al menos dos auditorías registradas.
- Solo se comparan auditorías ya registradas; no se ejecuta una auditoría nueva ni se consulta GitHub.
- Los estados Nuevo, Persistente y Corregido se calculan en la consulta y no se guardan.