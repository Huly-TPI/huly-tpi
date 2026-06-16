package com.huly.backend.infrastructure.presentation.dto.lantern;

import jakarta.validation.constraints.NotNull;

public record UpdateLanternStatusRequest(
        @NotNull(message = "El estado no puede ser nulo")
        String status
) {}
