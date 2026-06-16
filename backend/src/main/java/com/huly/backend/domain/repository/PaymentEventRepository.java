package com.huly.backend.domain.repository;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentEventRepository {
    PaymentEvent save(PaymentEvent event);
    Optional<PaymentEvent> findByMpPaymentId(Long mpPaymentId);
    Optional<PaymentEvent> findByExternalReference(String externalReference);
    List<PaymentEvent> findByUserId(Long userId);
    List<PaymentEvent> findByUserIdAndStatus(Long userId, PaymentStatus status);
    void updateStatus(Long id, PaymentStatus status, Long mpPaymentId, String errorDetail);
    boolean approveIfPending(Long id, Long mpPaymentId);
}
