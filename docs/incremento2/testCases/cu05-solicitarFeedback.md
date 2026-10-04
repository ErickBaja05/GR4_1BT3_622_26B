# Modelo de Pruebas - Casos de Uso (Flujo Básico)

---

## 2. Modelo de Pruebas - CU05: Solicitar retroalimentación de hallazgo

### Caso de prueba "Solicitar retroalimentación de hallazgo" - Flujo Básico (CP-CU05-01)
Especifica la entrada, los resultados esperados y otras condiciones relevantes para verificar el flujo básico del caso de uso "Solicitar retroalimentación de hallazgo".

- **Entradas:**
  - El Estudiante consulta el reporte de auditoría del proyecto académico.
  - El Estudiante especifica el hallazgo o observación realizada en la auditoría: "HLG-004 (Complejidad ciclomática alta)".
  - El hallazgo "HLG-004" no posee solicitudes de revisión previas registradas.
  - El Estudiante redacta la justificación técnica, duda o argumento de revisión: "La función requiere múltiples estructuras condicionales para el control de errores de entrada".

- **Resultados:**
  - Se verifica que el hallazgo no tiene solicitud previa, se crea la solicitud de retroalimentación para el hallazgo especificado y notifica la solicitud al docente.
  - El Estudiante recibe la confirmación del registro de la solicitud y del envío/entrega de la misma al docente.

- **Condiciones:**
  - Durante la creación de la solicitud, el estado del hallazgo se actualiza para evitar la apertura simultánea de otra revisión sobre el mismo ítem.