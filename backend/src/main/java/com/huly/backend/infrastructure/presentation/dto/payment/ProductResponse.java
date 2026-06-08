package com.huly.backend.infrastructure.presentation.dto.payment;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        String description,
        BigDecimal price
) {}
