package com.huly.backend.presentation.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChatHistoryPageResponse(
        List<ChatMessageResponse> content,
        @JsonProperty("page_number") int pageNumber,
        @JsonProperty("page_size") int pageSize,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages,
        boolean first,
        boolean last
) {}
