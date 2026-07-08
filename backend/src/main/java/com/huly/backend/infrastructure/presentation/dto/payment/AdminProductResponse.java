package com.huly.backend.infrastructure.presentation.dto.payment;

import java.math.BigDecimal;

public record AdminProductResponse(
        Long id, String name, String description, BigDecimal price, Integer coinsAmount,
        String type, String planCode, Integer chatDailyLimit, Integer audioDailyLimit, boolean active
) {}