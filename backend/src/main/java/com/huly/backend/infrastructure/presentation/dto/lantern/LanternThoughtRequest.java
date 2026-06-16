package com.huly.backend.infrastructure.presentation.dto.lantern;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LanternThoughtRequest(
        @NotBlank(message = "El texto no puede estar vacío")
        @Size(max = 100, message = "El texto no puede superar los 100 caracteres")
        String text
) {}
