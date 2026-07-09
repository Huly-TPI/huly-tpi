package com.huly.backend.infrastructure.presentation.dto.payment;

import com.huly.backend.domain.model.enums.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductWebRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull BigDecimal price,
        Integer coinsAmount,
        @NotNull ProductType type,
        String planCode,
        Integer chatDailyLimit,
        Integer audioDailyLimit
) {}