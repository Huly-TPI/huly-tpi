package com.huly.backend.domain.dto.mandala;

import java.util.List;

/**
 * Respuesta de dominio con el listado paginado de mandalas disponibles.
 */
public record ListAvailableMandalasResponse(
        List<MandalaItem> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
