package com.huly.backend.domain.useCase.admin.userFinancials;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentEventResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal productPrice,
        String externalReference,
        Long mpPaymentId,
        String status,
        Integer coinsAmount,
        String productType,
        Instant createdAt
) {
}
