package com.huly.backend.infrastructure.presentation.dto.user;

public record AudioSettingsResponse(
        Double interfaceVolume,
        Double ambientVolume,
        Double minigameVolume
) {
}
