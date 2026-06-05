package com.huly.backend.domain.model.vector;

import java.util.Map;

public record VectorMemory(
        String id,
        Long userId,
        VectorMemorySource sourceType,
        String sourceId,
        String content,
        Map<String, Object> metadata,
        Double score
) {
}
