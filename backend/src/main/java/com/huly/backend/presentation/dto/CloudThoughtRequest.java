package com.huly.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CloudThoughtRequest(
        @NotBlank(message = "El pensamiento no puede estar vacío")
        String thought
) {}
