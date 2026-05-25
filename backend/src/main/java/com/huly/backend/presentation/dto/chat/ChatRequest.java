package com.huly.backend.presentation.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank
        String message,

        @NotBlank
        String conversationId
){}
