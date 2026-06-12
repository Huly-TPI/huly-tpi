package com.huly.backend.infrastructure.presentation.dto.payment;

import java.math.BigDecimal;

public record PlanResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String planCode
) {}
