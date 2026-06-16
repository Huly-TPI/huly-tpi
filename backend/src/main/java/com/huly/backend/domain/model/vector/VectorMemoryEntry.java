package com.huly.backend.domain.model.vector;

public record VectorMemoryEntry(
        String id,
        String content,
        String sourceType,
        String contentType,
        String createdAt
) {
}
