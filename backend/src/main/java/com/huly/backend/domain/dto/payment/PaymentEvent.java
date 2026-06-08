package com.huly.backend.domain.dto.payment;

import com.huly.backend.domain.model.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class PaymentEvent {
    private Long id;
    private Long userId;
    private Long productId;
    private String mpPreferenceId;
    private Long mpPaymentId;
    private PaymentStatus status;
    private Integer coinsAmount;
    private String errorDetail;
    private Instant createdAt;
    private Instant updatedAt;
}
