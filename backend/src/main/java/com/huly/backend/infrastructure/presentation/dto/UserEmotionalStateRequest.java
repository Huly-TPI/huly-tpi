package com.huly.backend.infrastructure.presentation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserEmotionalStateRequest (

    @NotNull
    Long userId,

    @NotNull
    @DecimalMin("-1.0") @DecimalMax("1.0")
    Double valence,

    @NotNull
    @DecimalMin("-1.0") @DecimalMax("1.0")
    Double arousal,

    @NotNull
    @DecimalMin("-1.0") @DecimalMax("1.0")
    Double dominance,

    @NotNull
    @DecimalMin("0.0") @DecimalMax("1.0")
    Double intensity,

    @NotBlank
    String source

)
{}