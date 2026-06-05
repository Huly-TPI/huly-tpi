package com.huly.backend.presentation.dto.journal;

import jakarta.validation.constraints.NotBlank;

public record JournalEntryRequest(
        @NotBlank(message = "El contenido no puede estar vacío")
        String content,
        String mood,
        Boolean useTextForAI
) {}
