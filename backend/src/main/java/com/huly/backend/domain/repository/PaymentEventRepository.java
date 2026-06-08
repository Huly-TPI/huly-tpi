package com.huly.backend.domain.repository;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;

import java.util.Optional;

public interface PaymentEventRepository {
    PaymentEvent save(PaymentEvent event);
    Optional<PaymentEvent> findByMpPaymentId(Long mpPaymentId);
    Optional<PaymentEvent> findByMpPreferenceId(String mpPreferenceId);
    void updateStatus(Long id, PaymentStatus status, Long mpPaymentId, String errorDetail);
}
