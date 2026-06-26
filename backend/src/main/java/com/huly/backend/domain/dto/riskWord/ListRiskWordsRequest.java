package com.huly.backend.domain.dto.riskWord;

/**
 * Pedido de dominio para listar palabras de riesgo con filtros y paginacion.
 *
 * @param word     fragmento para filtrar por contenido de la palabra (puede ser {@code null}).
 * @param active   estado activo/inactivo para filtrar (puede ser {@code null}).
 * @param severity nivel de severidad como texto para filtrar (puede ser {@code null}).
 * @param page     numero de pagina comenzando en 0.
 * @param size     cantidad de elementos por pagina.
 */
public record ListRiskWordsRequest(String word, Boolean active, String severity, int page, int size) {
}
