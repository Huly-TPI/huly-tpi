package com.huly.backend.infrastructure.presentation.dto.cloudRecommendation;

import java.time.Instant;

public record CloudThoughtResponse(
        Long id,
        String text,
        boolean workedOn,
        Instant createdAt
) {}
