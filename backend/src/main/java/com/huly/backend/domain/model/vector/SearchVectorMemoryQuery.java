package com.huly.backend.domain.model.vector;

public record SearchVectorMemoryQuery(
        Long userId,
        VectorMemorySource sourceType,
        String query,
        Integer limit,
        Double similarityThreshold
) {
}
