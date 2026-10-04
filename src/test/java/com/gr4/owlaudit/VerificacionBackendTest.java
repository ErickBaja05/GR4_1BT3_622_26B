package com.gr4.owlaudit;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.model.Usuario;
import com.gr4.owlaudit.service.ProyectoService;
import com.gr4.owlaudit.service.ProyectoServiceImpl;
import com.gr4.owlaudit.service.ReglaService;
import com.gr4.owlaudit.service.ReglaServiceImpl;
import com.gr4.owlaudit.service.UsuarioService;
import com.gr4.owlaudit.service.UsuarioServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Suite de Pruebas de Verificación para CU01, CU02 y Autenticación.
 * Valida la alineación 100% con clases.puml, secuencia.puml y secuencia2.puml.
 */
public class VerificacionBackendTest {

    // ========================================================
    // Mock DAOs basados estrictamente en las firmas de clases.puml
    // ========================================================

    static class MockUsuarioDAO implements UsuarioDAO {
        private final List<Usuario> usuarios = new ArrayList<>();

        public MockUsuarioDAO() {
            usuarios.add(new Usuario(1L, "Profesor Titular", "docente@epn.edu.ec", "docente123", RolEnum.DOCENTE));
            usuarios.add(new Usuario(2L, "Estudiante Uno", "estudiante1@epn.edu.ec", "estudiante123", RolEnum.ESTUDIANTE));
            usuarios.add(new Usuario(3L, "Estudiante Dos", "estudiante2@epn.edu.ec", "estudiante456", RolEnum.ESTUDIANTE));
        }

        @Override
        public Usuario buscarPorCorreo(String correo) {
            return usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo.trim()))
                .findFirst().orElse(null);
        }

        @Override
        public Usuario buscarPorCredenciales(String correo, String contrasena) {
            return usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(correo.trim()) && u.getContrasena().equals(contrasena))
                .findFirst().orElse(null);
        }

        @Override
        public void guardar(Usuario usuario) {
            usuario.setId((long) (usuarios.size() + 1));
            usuarios.add(usuario);
        }
    }

    static class MockReglaDAO implements ReglaDAO {
        private final List<Regla> reglas = new ArrayList<>();

        @Override
        public boolean existePorNombre(String nombre) {
            return reglas.stream()
                .anyMatch(r -> r.getNombreRepresentativo().equalsIgnoreCase(nombre.trim()));
        }

        @Override
        public void guardar(Regla regla) {
            regla.setId((long) (reglas.size() + 1));
            reglas.add(regla);
        }

        @Override
        public List<Regla> consultarReglasDeEvaluacionVigentes() {
            return reglas.stream().filter(Regla::isActiva).toList();
        }
    }

    static class MockProyectoDAO implements ProyectoDAO {
        private final List<Proyecto> proyectos = new ArrayList<>();

        @Override
        public void guardarProyectoAcademico(Proyecto proyecto) {
            proyecto.setId((long) (proyectos.size() + 1));
            proyectos.add(proyecto);
        }

        public List<Proyecto> getProyectos() {
            return proyectos;
        }
    }

    // ========================================================
    // CASOS DE PRUEBA
    // ========================================================

    public static void testAutenticacionExitosaDocente() {
        UsuarioService service = new UsuarioServiceImpl(new MockUsuarioDAO());
        UsuarioDTO dto = service.autenticar("docente@epn.edu.ec", "docente123");
        assertNotNull(dto, "El DTO del usuario no debe ser nulo.");
        assertEquals(RolEnum.DOCENTE, dto.getRol(), "El rol debe ser DOCENTE.");
        assertEquals("docente@epn.edu.ec", dto.getCorreo(), "El correo debe coincidir.");
        assertEquals("Profesor Titular", dto.getNombre(), "El nombre debe coincidir.");
        System.out.println("  [PASS] testAutenticacionExitosaDocente: Docente autenticado con RolEnum.DOCENTE.");
    }

    public static void testAutenticacionExitosaEstudiante() {
        UsuarioService service = new UsuarioServiceImpl(new MockUsuarioDAO());
        UsuarioDTO dto = service.autenticar("estudiante1@epn.edu.ec", "estudiante123");
        assertNotNull(dto, "El DTO del usuario no debe ser nulo.");
        assertEquals(RolEnum.ESTUDIANTE, dto.getRol(), "El rol debe ser ESTUDIANTE.");
        System.out.println("  [PASS] testAutenticacionExitosaEstudiante: Estudiante autenticado con RolEnum.ESTUDIANTE.");
    }

    public static void testAutenticacionFallidaContrasenaIncorrecta() {
        UsuarioService service = new UsuarioServiceImpl(new MockUsuarioDAO());
        try {
            service.autenticar("docente@epn.edu.ec", "claveInvalida");
            throw new AssertionError("Debió lanzar ExcepcionNegocio por credenciales inválidas.");
        } catch (ExcepcionNegocio e) {
            System.out.println("  [PASS] testAutenticacionFallidaContrasenaIncorrecta: Rechazado correctamente (" + e.getMessage() + ")");
        }
    }

    public static void testRegistrarReglaExitosa() {
        MockReglaDAO dao = new MockReglaDAO();
        ReglaService service = new ReglaServiceImpl(dao);

        NuevaReglaDTO dto = new NuevaReglaDTO(
            "PRESENCIA_ARCHIVO",
            "Verificar README obligatorio",
            "Comprueba que el repositorio contenga un archivo README.md",
            "README.md",
            "ALTA",
            5
        );

        service.registrarReglaEvaluacionTecnica(dto);

        List<Regla> vigentes = dao.consultarReglasDeEvaluacionVigentes();
        assertEquals(1, vigentes.size(), "Debe haber 1 regla registrada en el DAO.");
        Regla r = vigentes.get(0);
        assertEquals("Verificar README obligatorio", r.getNombreRepresentativo(), "Nombre debe coincidir.");
        assertEquals(TipoMotorEnum.PRESENCIA_ARCHIVO, r.getTipoMotor(), "Tipo motor debe coincidir.");
        assertEquals(NivelSeveridadEnum.ALTA, r.getNivelSeveridad(), "Severidad debe coincidir.");
        assertEquals(5, r.getPonderacion(), "Ponderación debe coincidir.");
        assertTrue(r.isActiva(), "La regla debe quedar activa.");
        System.out.println("  [PASS] testRegistrarReglaExitosa: Regla registrada y activada según secuencia.puml.");
    }

    public static void testRegistrarReglaNombreDuplicado() {
        MockReglaDAO dao = new MockReglaDAO();
        ReglaService service = new ReglaServiceImpl(dao);

        NuevaReglaDTO dto1 = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Regla Duplicada", "Desc", "README.md", "MEDIA", 3);
        service.registrarReglaEvaluacionTecnica(dto1);

        NuevaReglaDTO dto2 = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Regla Duplicada", "Desc 2", "LICENSE", "BAJA", 2);
        try {
            service.registrarReglaEvaluacionTecnica(dto2);
            throw new AssertionError("Debió lanzar ExcepcionNegocio por nombre duplicado.");
        } catch (ExcepcionNegocio e) {
            System.out.println("  [PASS] testRegistrarReglaNombreDuplicado: Unicidad validada (" + e.getMessage() + ")");
        }
    }

    public static void testRegistrarReglaPonderacionInvalida() {
        MockReglaDAO dao = new MockReglaDAO();
        ReglaService service = new ReglaServiceImpl(dao);

        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Regla Ponderacion Cero", "Desc", "README.md", "MEDIA", 0);
        try {
            service.registrarReglaEvaluacionTecnica(dto);
            throw new AssertionError("Debió lanzar ExcepcionNegocio por ponderación <= 0.");
        } catch (ExcepcionNegocio e) {
            System.out.println("  [PASS] testRegistrarReglaPonderacionInvalida: Ponderación rechazada (" + e.getMessage() + ")");
        }
    }

    public static void testRegistrarReglaParametroInvalido() {
        MockReglaDAO dao = new MockReglaDAO();
        ReglaService service = new ReglaServiceImpl(dao);

        // Ruta de archivo no puede empezar con '/'
        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Ruta Invalida", "Desc", "/src/Main.java", "MEDIA", 5);
        try {
            service.registrarReglaEvaluacionTecnica(dto);
            throw new AssertionError("Debió lanzar ExcepcionNegocio por ruta con '/' inicial.");
        } catch (ExcepcionNegocio e) {
            System.out.println("  [PASS] testRegistrarReglaParametroInvalido: Formato de parámetro rechazado (" + e.getMessage() + ")");
        }
    }

    public static void testRegistrarProyectoExitoso() {
        MockProyectoDAO dao = new MockProyectoDAO();
        ProyectoService service = new ProyectoServiceImpl(dao);

        NuevoProyectoDTO dto = new NuevoProyectoDTO(
            "Sistema de Gestión Escolar",
            "https://github.com/org/proyecto-escolar"
        );

        service.solicitarRegistroDeNuevoProyecto(dto);

        assertEquals(1, dao.getProyectos().size(), "El proyecto debe haberse guardado en el DAO.");
        Proyecto guardado = dao.getProyectos().get(0);
        assertEquals("Sistema de Gestión Escolar", guardado.getNombre(), "Nombre del proyecto debe coincidir.");
        assertEquals("https://github.com/org/proyecto-escolar", guardado.getUrlGithub(), "URL debe coincidir.");
        assertNotNull(guardado.getFechaRegistro(), "La fecha de registro debe estar asignada.");
        System.out.println("  [PASS] testRegistrarProyectoExitoso: Proyecto registrado según secuencia2.puml.");
    }

    public static void testRegistrarProyectoEnlaceInvalido() {
        MockProyectoDAO dao = new MockProyectoDAO();
        ProyectoService service = new ProyectoServiceImpl(dao);

        NuevoProyectoDTO dto = new NuevoProyectoDTO(
            "Proyecto Con Enlace Roto",
            "http://enlace-no-valido-a-github.com"
        );

        try {
            service.solicitarRegistroDeNuevoProyecto(dto);
            throw new AssertionError("Debió lanzar ExcepcionNegocio con 'Enlace no válido'.");
        } catch (ExcepcionNegocio e) {
            assertEquals("Enlace no válido", e.getMessage(), "El mensaje debe ser 'Enlace no válido' según secuencia2.puml");
            System.out.println("  [PASS] testRegistrarProyectoEnlaceInvalido: Rechazado con mensaje exacto: '" + e.getMessage() + "'");
        }
    }

    // ========================================================
    // RUNNER PRINCIPAL (MAIN)
    // ========================================================
    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println(" INICIANDO PRUEBAS DE VERIFICACIÓN - BACKEND (CU01 / CU02 / LOGIN)");
        System.out.println("========================================================");

        int total = 0;
        int exitosos = 0;

        Runnable[] tests = {
            VerificacionBackendTest::testAutenticacionExitosaDocente,
            VerificacionBackendTest::testAutenticacionExitosaEstudiante,
            VerificacionBackendTest::testAutenticacionFallidaContrasenaIncorrecta,
            VerificacionBackendTest::testRegistrarReglaExitosa,
            VerificacionBackendTest::testRegistrarReglaNombreDuplicado,
            VerificacionBackendTest::testRegistrarReglaPonderacionInvalida,
            VerificacionBackendTest::testRegistrarReglaParametroInvalido,
            VerificacionBackendTest::testRegistrarProyectoExitoso,
            VerificacionBackendTest::testRegistrarProyectoEnlaceInvalido
        };

        for (Runnable test : tests) {
            total++;
            try {
                test.run();
                exitosos++;
            } catch (Throwable t) {
                System.err.println("  [FAIL] " + t.getMessage());
                t.printStackTrace();
            }
        }

        System.out.println("========================================================");
        System.out.printf(" RESULTADO: %d de %d pruebas superadas exitosamente.%n", exitosos, total);
        System.out.println("========================================================");

        if (exitosos != total) {
            System.exit(1);
        }
    }

    // Aserciones auxiliares
    private static void assertEquals(Object expected, Object actual, String mensaje) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(mensaje + " Esperado: <" + expected + ">, pero fue: <" + actual + ">");
    }

    private static void assertNotNull(Object obj, String mensaje) {
        if (obj == null) {
            throw new AssertionError(mensaje + " El objeto era nulo.");
        }
    }

    private static void assertTrue(boolean condition, String mensaje) {
        if (!condition) {
            throw new AssertionError(mensaje + " La condición no se cumplió (false).");
        }
    }
}
