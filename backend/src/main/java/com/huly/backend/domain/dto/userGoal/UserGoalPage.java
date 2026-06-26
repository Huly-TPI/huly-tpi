package com.huly.backend.domain.dto.userGoal;

import java.util.List;

/**
 * Pagina de dominio con metas de usuario y metadatos de paginacion.
 * No referencia tipos de Spring Data.
 */
public record UserGoalPage(
        List<UserGoalItem> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
