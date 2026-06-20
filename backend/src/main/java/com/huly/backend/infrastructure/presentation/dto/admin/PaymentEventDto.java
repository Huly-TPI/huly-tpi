package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class PaymentEventDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String externalReference;
    private Long mpPaymentId;
    private String status;
    private Integer coinsAmount;
    private String productType;
    private Instant createdAt;
}
