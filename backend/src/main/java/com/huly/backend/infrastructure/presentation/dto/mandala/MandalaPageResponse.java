package com.huly.backend.infrastructure.presentation.dto.mandala;

import java.util.List;

public record MandalaPageResponse(
        List<MandalaResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
