package com.huly.backend.domain.model.vector;

import java.util.List;

public record SearchVectorMemoriesQuery(
        Long userId,
        List<VectorMemorySource> sourceTypes,
        String query,
        Integer limit,
        Double similarityThreshold
) {
}
