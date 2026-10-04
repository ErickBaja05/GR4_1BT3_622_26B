## CASO DE PRUEBA DE EJECUTAR AUDITORÍA DE CALIDAD

* **Identificador:** `CP-CU03-01`
* **Caso de Uso Asociado:** `ejecutarAuditoriaCalidad`
* **Nombre de la Prueba:** Ejecución exitosa de auditoría de calidad con cumplimiento parcial de las reglas activas
* **Objetivo:** Verificar que el sistema obtenga la estructura del repositorio desde la API de GitHub, evalúe cada regla activa, calcule el puntaje obtenido sobre el puntaje máximo con su porcentaje y registre la auditoría con el resultado de cada regla y un hallazgo por cada incumplimiento

**Entrada**

- El estudiante solicita auditar su proyecto "Proyecto Demo" (github.com/estudiante-demo/proyecto-demo).
- El sistema informa las 4 reglas activas: Archivo gitignore (20, ALTA), Archivo README (5, BAJA), Carpeta src (5, MEDIA) y Sin archivos env (5, ALTA), con un puntaje máximo de 35.
- El estudiante confirma la auditoría.
- El sistema solicita a GitHub la estructura del repositorio.
- GitHub entrega la estructura: .gitignore, README.md, .env y docs/.

**Salida**

- Se evalúa cada regla: Archivo gitignore y Archivo README cumplen; Carpeta src y Sin archivos env no cumplen.
- Se calcula el puntaje obtenido: 25 de 35 (71,4 %).
- Se registra la auditoría con el resultado de las 4 reglas.
- Se registran 2 hallazgos, cada uno con su severidad, evidencia y recomendación: Carpeta src (MEDIA) y Sin archivos env (ALTA).
- Se muestra al estudiante el puntaje, el porcentaje, el resultado de cada regla y los hallazgos.

**Condiciones**

- El estudiante debe estar autenticado y el proyecto debe estar registrado a su nombre.
- Deben existir estas 4 reglas activas (tipo de motor, parámetro, ponderación y severidad):
    - Archivo gitignore: PRESENCIA_ARCHIVO, `.gitignore`, 20, ALTA.
    - Archivo README: PRESENCIA_ARCHIVO, `README.md`, 5, BAJA.
    - Carpeta src: ESTRUCTURA_CARPETAS, `src`, 5, MEDIA.
    - Sin archivos env: RESTRICCION_ARCHIVOS, `.env`, 5, ALTA.
- El repositorio debe ser público y accesible desde la API de GitHub.
- Solo se genera un hallazgo por cada regla que no se cumple.
- La auditoría registrada no se puede modificar.