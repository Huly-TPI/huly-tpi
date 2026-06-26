package com.huly.backend.domain.dto.riskWord;

import java.util.List;

/**
 * Respuesta de dominio con el listado paginado de palabras de riesgo.
 *
 * @param content       palabras de riesgo de la pagina actual.
 * @param pageNumber    numero de pagina actual (comienza en 0).
 * @param pageSize      cantidad maxima de elementos por pagina.
 * @param totalElements total de registros que coinciden con los filtros aplicados.
 * @param totalPages    total de paginas disponibles.
 * @param first         {@code true} si es la primera pagina.
 * @param last          {@code true} si es la ultima pagina.
 */
public record ListRiskWordsResponse(
        List<RiskWordItem> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
