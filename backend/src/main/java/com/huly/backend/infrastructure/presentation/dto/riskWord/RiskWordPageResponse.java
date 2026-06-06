package com.huly.backend.infrastructure.presentation.dto.riskWord;

import java.util.List;

/**
 * DTO de salida para la respuesta paginada del listado de palabras de riesgo.
 * Encapsula los datos de paginación sin exponer directamente la clase
 * {@link org.springframework.data.domain.Page} de Spring al cliente.
 *
 * @param content        lista de palabras de riesgo de la página actual
 * @param pageNumber     número de página actual (comienza en 0)
 * @param pageSize       cantidad máxima de elementos por página
 * @param totalElements  total de registros que coinciden con los filtros aplicados
 * @param totalPages     total de páginas disponibles
 * @param first          {@code true} si es la primera página
 * @param last           {@code true} si es la última página
 */
public record RiskWordPageResponse(
        List<RiskWordResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {}
