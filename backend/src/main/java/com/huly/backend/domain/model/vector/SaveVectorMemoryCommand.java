package com.huly.backend.domain.model.vector;

import java.util.Map;

public record SaveVectorMemoryCommand(
        Long userId,
        VectorMemorySource sourceType,
        String sourceId,
        String source,
        String contentType,
        String content,
        String conversationId,
        String messageId,
        Map<String, Object> metadata
) {
}
