package com.huly.backend.infrastructure.presentation.dto.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateAudioSettingsRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double interfaceVolume,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double ambientVolume,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double minigameVolume
) {
}
