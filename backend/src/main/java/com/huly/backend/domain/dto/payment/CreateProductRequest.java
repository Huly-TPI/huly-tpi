package com.huly.backend.domain.dto.payment;

import com.huly.backend.domain.model.enums.ProductType;
import java.math.BigDecimal;

public record CreateProductRequest(
        String name, String description, BigDecimal price, Integer coinsAmount,
        ProductType type, String planCode, Integer chatDailyLimit, Integer audioDailyLimit
) {}