package com.huly.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank
        String message,

        @NotBlank
        String conversationId
){}
