# Modelo de Pruebas - Casos de Uso (Flujo Básico)

---

## 1. Modelo de Pruebas - CU02: Registrar proyecto académico

### Caso de prueba "Registrar proyecto académico" - Flujo Básico (CP-CU02-01)
Especifica la entrada, los resultados esperados y otras condiciones relevantes para verificar el flujo básico del caso de uso "Registrar proyecto académico".

- **Entradas:**
  - El Estudiante se identifica correctamente como usuario estudiante en la plataforma.
  - El Estudiante ingresa el nombre del proyecto: "Sistema de Gestión Escolar".
  - El Estudiante proporciona el enlace del repositorio de GitHub: "https://github.com/org/proyecto-escolar".
  - El enlace proporcionado es sintácticamente válido y corresponde a un repositorio público con archivos y carpetas legibles.

- **Resultados:**
  - Se comprueba el enlace, se confirma que el repositorio es público, se extrae la información (carpetas, archivos) y se guarda el proyecto con su información académica.
  - El Estudiante recibe la confirmación de que se registró exitosamente el proyecto.

- **Condiciones:**
  - No se permite registrar de manera simultánea otro proyecto académico asociado al mismo enlace de repositorio durante la ejecución del caso de prueba.

