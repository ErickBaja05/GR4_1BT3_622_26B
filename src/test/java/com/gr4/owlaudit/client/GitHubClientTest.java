package com.gr4.owlaudit.client;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prueba de integración: requiere internet (API pública de GitHub, 60 peticiones/hora sin token). */
@Tag("integracion")
class GitHubClientTest {

    private static final String MENSAJE_NO_ACCESIBLE =
        "No fue posible auditar el proyecto: el repositorio no existe o no es público";

    private final GitHubClient cliente = new GitHubClient();

    @Test
    void repositorioPublicoDevuelveSuEstructura() {
        List<String> rutas = cliente.obtenerEstructuraDelRepositorio("https://github.com/octocat/Hello-World");

        System.out.println("[GitHub] octocat/Hello-World -> " + rutas);
        assertFalse(rutas.isEmpty());
        assertTrue(rutas.contains("README"));
    }

    @Test
    void alterno3_repositorioInexistente() {
        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class,
            () -> cliente.obtenerEstructuraDelRepositorio("https://github.com/owl-audit-no-existe/repo-inexistente-123"));

        System.out.println("[GitHub] repositorio inexistente -> " + error.getMessage());
        assertEquals(MENSAJE_NO_ACCESIBLE, error.getMessage());
    }

    @Test
    void alterno3_enlaceQueNoEsDeGitHub() {
        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class,
            () -> cliente.obtenerEstructuraDelRepositorio("https://gitlab.com/usuario/repo"));

        assertEquals(MENSAJE_NO_ACCESIBLE, error.getMessage());
    }
}
