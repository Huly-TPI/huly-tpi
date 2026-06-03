package com.huly.backend.presentation.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatChallengeDecisionRequest(
        @NotBlank String conversationId,
        @NotBlank String title,
        String description,
        @NotBlank String decision
) {
}
