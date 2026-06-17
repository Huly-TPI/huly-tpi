package com.huly.backend.domain.useCase.admin.userAiDiagnostics;

public record VectorMemoryResponse(
        String id,
        String content,
        String sourceType,
        String contentType,
        String createdAt
) {
}
