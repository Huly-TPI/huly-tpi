package com.huly.backend.domain.model.vector;

public record DeleteVectorMemoryCommand(
        Long userId,
        VectorMemorySource sourceType,
        String sourceId
) {
}
