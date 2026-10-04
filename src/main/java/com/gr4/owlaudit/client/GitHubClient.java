package com.gr4.owlaudit.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Frontera con el sistema externo GitHub (actor secundario de CU03).
 * Devuelve las rutas del repositorio relativas a la raíz; las carpetas terminan en "/".
 */
public class GitHubClient {

    private static final String MENSAJE_NO_ACCESIBLE =
        "No fue posible auditar el proyecto: el repositorio no existe o no es público";

    // https://github.com/owner/repo, con o sin "https://", "www.", ".git" y "/" final
    private static final Pattern PATRON_URL = Pattern.compile(
        "^(?:https?://)?(?:www\\.)?github\\.com/([A-Za-z0-9-]+)/([A-Za-z0-9._-]+?)(?:\\.git)?/?$"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> obtenerEstructuraDelRepositorio(String url) {
        if (url == null) {
            throw new ExcepcionNegocio(MENSAJE_NO_ACCESIBLE);
        }
        Matcher matcher = PATRON_URL.matcher(url.trim());
        if (!matcher.matches()) {
            throw new ExcepcionNegocio(MENSAJE_NO_ACCESIBLE);
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/git/trees/HEAD?recursive=1"))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "OwlAuditApp")
            .GET();
        // Opcional: sube el límite de la API de 60 a 5000 peticiones por hora
        String token = System.getenv("GITHUB_TOKEN");
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> respuesta;
        try {
            respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ExcepcionNegocio(MENSAJE_NO_ACCESIBLE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionNegocio(MENSAJE_NO_ACCESIBLE, e);
        }

        // 404: no existe o es privado; 403: límite de peticiones; 409: repositorio vacío
        if (respuesta.statusCode() != 200) {
            throw new ExcepcionNegocio(MENSAJE_NO_ACCESIBLE);
        }

        try {
            JsonNode raiz = objectMapper.readTree(respuesta.body());
            if (raiz.path("truncated").asBoolean(false)) {
                System.out.println("ADVERTENCIA: GitHub truncó la estructura del repositorio " + owner + "/" + repo);
            }
            List<String> rutas = new ArrayList<>();
            for (JsonNode elemento : raiz.path("tree")) {
                String ruta = elemento.path("path").asText();
                if ("tree".equals(elemento.path("type").asText())) {
                    ruta = ruta + "/";
                }
                rutas.add(ruta);
            }
            return rutas;
        } catch (IOException e) {
            throw new ExcepcionNegocio(MENSAJE_NO_ACCESIBLE, e);
        }
    }
}
