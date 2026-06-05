package com.huly.backend.infrastructure.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CloudThoughtRequest(
        @NotBlank(message = "El pensamiento no puede estar vacío")
        String thought
) {}
