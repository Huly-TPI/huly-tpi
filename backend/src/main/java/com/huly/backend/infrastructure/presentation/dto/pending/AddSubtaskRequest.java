package com.huly.backend.infrastructure.presentation.dto.pending;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSubtaskRequest(
        @NotBlank(message = "El texto no puede estar vacío")
        @Size(max = 200, message = "El texto no puede superar los 200 caracteres")
        String text
) {}
