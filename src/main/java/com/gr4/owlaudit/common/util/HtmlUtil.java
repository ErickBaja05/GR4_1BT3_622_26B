package com.gr4.owlaudit.common.util;

/**
 * Utilidad para escapar texto antes de imprimirlo en los JSP (evita XSS),
 * ya que el proyecto no utiliza JSTL.
 */
public final class HtmlUtil {

    private HtmlUtil() {
    }

    public static String escape(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = String.valueOf(valor);
        StringBuilder sb = new StringBuilder(texto.length());
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}
