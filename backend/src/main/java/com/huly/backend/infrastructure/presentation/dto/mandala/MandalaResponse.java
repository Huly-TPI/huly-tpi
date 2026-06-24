package com.huly.backend.infrastructure.presentation.dto.mandala;

public record MandalaResponse(
        String id,
        String title,
        String description,
        String assetKey,
        int displayOrder,
        String unlockSource
) {
}
