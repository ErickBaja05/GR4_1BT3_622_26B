package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO.ReglaComparadaDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO.AuditoriaItemDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CU04 - Consultar Evolución de Calidad (roles ESTUDIANTE y DOCENTE).
 * Basado en diagramaClasesIncremento2.puml y secuencia4.puml.
 */
@WebServlet("/evolucion")
public class EvolucionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/consultarEvolucion.jsp";

    // 1. Carga del Historial (doGet) según secuencia4.puml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String proyectoIdStr = request.getParameter("proyectoId");
        Long proyectoId = parseLongOrNull(proyectoIdStr);

        // Si no se especifica proyectoId, se utiliza uno de prueba/demostración (ID: 1)
        if (proyectoId == null) {
            proyectoId = 1L;
        }

        // Armado del DTO correspondiente
        EvolucionDTO evolucionDTO = new EvolucionDTO(proyectoId);
        request.setAttribute("evolucionDTO", evolucionDTO);

        // Salida por consola para trazabilidad y pruebas rápidas
        System.out.println("==================================================================");
        System.out.println("[EvolucionServlet - GET] 1. Carga del Historial de Auditorías");
        System.out.println("  -> DTO generado: " + evolucionDTO);
        System.out.println("  -> Proyecto ID a consultar: " + proyectoId);
        System.out.println("==================================================================");

        // Generación del ResumenHistorialDTO para presentar las auditorías registradas
        ResumenHistorialDTO resumenHistorial = obtenerHistorialAuditoriasDemo(proyectoId);
        request.setAttribute("resumenHistorialDTO", resumenHistorial);

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // 2. Comparación de Auditorías (doPost) según secuencia4.puml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        String proyectoIdStr = request.getParameter("proyectoId");
        String baseIdStr = request.getParameter("baseId");
        String comparadaIdStr = request.getParameter("comparadaId");

        Long proyectoId = parseLongOrNull(proyectoIdStr);
        Long baseId = parseLongOrNull(baseIdStr);
        Long comparadaId = parseLongOrNull(comparadaIdStr);

        if (proyectoId == null) {
            proyectoId = 1L;
        }

        // 1. Instanciación y armado del DTO según Diagrama de Clases
        EvolucionDTO evolucionDTO = new EvolucionDTO(proyectoId, baseId, comparadaId);
        request.setAttribute("evolucionDTO", evolucionDTO);

        // Salida a consola para pruebas y verificación
        System.out.println("==================================================================");
        System.out.println("[EvolucionServlet - POST] 2. Comparación de Dos Auditorías");
        System.out.println("  -> DTO armado: " + evolucionDTO);
        System.out.println("  -> Proyecto ID: " + proyectoId);
        System.out.println("  -> Auditoría Base ID: " + baseId);
        System.out.println("  -> Auditoría Comparada ID: " + comparadaId);
        System.out.println("==================================================================");

        // Mantener el historial disponible para la vista
        ResumenHistorialDTO resumenHistorial = obtenerHistorialAuditoriasDemo(proyectoId);
        request.setAttribute("resumenHistorialDTO", resumenHistorial);

        // Validaciones de negocio (según el caso de uso consultarEvolucionCalidad.md)
        if (baseId == null || comparadaId == null) {
            request.setAttribute("mensajeError", "Debe seleccionar dos auditorías para realizar la comparación.");
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        if (baseId.equals(comparadaId)) {
            request.setAttribute("mensajeError", "Debe indicar dos auditorías distintas.");
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        // Ordenamiento por fecha (la más antigua como base y la más reciente como comparada)
        if (baseId > comparadaId) {
            Long temp = baseId;
            baseId = comparadaId;
            comparadaId = temp;
            evolucionDTO.setBaseId(baseId);
            evolucionDTO.setComparadaId(comparadaId);
            System.out.println("[EvolucionServlet] Auditorías ordenadas cronológicamente: Base ID=" + baseId + ", Comparada ID=" + comparadaId);
        }

        // Armado del resultado de comparación en EvolucionFinalDTO
        EvolucionFinalDTO evolucionFinal = armarEvolucionFinalDemo(proyectoId, baseId, comparadaId);
        request.setAttribute("evolucionFinalDTO", evolucionFinal);
        request.setAttribute("mensajeExito", "Evolución de calidad calculada exitosamente.");

        System.out.println("[EvolucionServlet] Resultado generado: " + evolucionFinal);

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private Long parseLongOrNull(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Provee el historial estructurado de auditorías para realizar pruebas y visualización.
     */
    private ResumenHistorialDTO obtenerHistorialAuditoriasDemo(Long proyectoId) {
        ResumenHistorialDTO resumen = new ResumenHistorialDTO(proyectoId, "Sistema de Gestión Académica - GR4");
        List<AuditoriaItemDTO> lista = new ArrayList<>();
        lista.add(new AuditoriaItemDTO(101L, "2026-09-15 10:30:00", 65, 100, 65.0));
        lista.add(new AuditoriaItemDTO(102L, "2026-09-22 14:15:00", 80, 100, 80.0));
        lista.add(new AuditoriaItemDTO(103L, "2026-10-01 16:45:00", 90, 100, 90.0));
        resumen.setAuditorias(lista);
        return resumen;
    }

    /**
     * Provee el cálculo simulado de variación y clasificación de hallazgos
     * según las reglas del caso de uso ("Nuevo", "Corregido", "Persistente").
     */
    private EvolucionFinalDTO armarEvolucionFinalDemo(Long proyectoId, Long baseId, Long comparadaId) {
        EvolucionFinalDTO resultado = new EvolucionFinalDTO();
        resultado.setProyectoId(proyectoId);
        resultado.setNombreProyecto("Sistema de Gestión Académica - GR4");
        resultado.setBaseId(baseId);
        resultado.setComparadaId(comparadaId);
        resultado.setFechaBase("2026-09-15 10:30");
        resultado.setFechaComparada("2026-10-01 16:45");

        resultado.setPuntajeBase(65);
        resultado.setPuntajeComparada(90);
        resultado.setVariacionPuntaje(90 - 65); // +25 pts

        resultado.setPorcentajeBase(65.0);
        resultado.setPorcentajeComparada(90.0);
        resultado.setVariacionPorcentaje(25.0); // +25.0%

        // Clasificación de reglas
        List<ReglaComparadaDTO> detalles = new ArrayList<>();
        // Regla 1: Corregido (antes no cumplía, ahora sí cumple)
        detalles.add(new ReglaComparadaDTO(201L, "Presencia de archivo README.md en raíz", "MEDIA", false, true, 0, 15, "Corregido"));
        // Regla 2: Persistente (no cumplía y sigue sin cumplir)
        detalles.add(new ReglaComparadaDTO(202L, "Restricción de ejecutables y binarios (.exe, .jar)", "ALTA", false, false, 0, 0, "Persistente"));
        // Regla 3: Nuevo (antes cumplía pero en la comparada se detectó nuevo incumplimiento)
        detalles.add(new ReglaComparadaDTO(203L, "Estructura estándar de carpetas /src/main/java", "MEDIA", true, false, 20, 0, "Nuevo"));
        // Regla 4: Corregido
        detalles.add(new ReglaComparadaDTO(204L, "Presencia de archivo .gitignore configurado", "BAJA", false, true, 0, 10, "Corregido"));

        resultado.setDetallesReglas(detalles);
        resultado.setTotalCorregidos(2);
        resultado.setTotalPersistentes(1);
        resultado.setTotalNuevos(1);

        return resultado;
    }
}
