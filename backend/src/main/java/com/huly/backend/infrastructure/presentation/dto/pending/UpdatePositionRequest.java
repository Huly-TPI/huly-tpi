package com.huly.backend.infrastructure.presentation.dto.pending;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdatePositionRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double positionX,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double positionY
) {}
