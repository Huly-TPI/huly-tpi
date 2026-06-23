package com.huly.backend.domain.model.payment;

import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PaymentEvent {
    private Long id;
    private Long userId;
    private Long productId;
    private Long storeItemId;
    private String externalReference;
    private String mpPreferenceId;
    private Long mpPaymentId;
    private PaymentStatus status;
    private Integer coinsAmount;
    private ProductType productType;
    private String errorDetail;
    private Instant createdAt;
    private Instant updatedAt;
}
