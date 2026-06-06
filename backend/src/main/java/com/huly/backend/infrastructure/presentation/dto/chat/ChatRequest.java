package com.huly.backend.infrastructure.presentation.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank
        String message,

        @NotBlank
        String conversationId
){}
