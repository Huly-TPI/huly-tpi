package com.huly.backend.infrastructure.presentation.dto.lantern;

import java.time.Instant;

public record LanternThoughtResponse(
        Long id,
        String text,
        boolean workedOn,
        Instant createdAt
) {}
