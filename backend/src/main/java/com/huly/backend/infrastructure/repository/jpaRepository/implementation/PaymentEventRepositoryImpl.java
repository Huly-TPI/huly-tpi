package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.infrastructure.repository.entity.PaymentEventEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPaymentEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentEventRepositoryImpl implements PaymentEventRepository {

    private final IPaymentEventJpaRepository jpaRepository;

    @Override
    public PaymentEvent save(PaymentEvent event) {
        return toDomain(jpaRepository.save(toEntity(event)));
    }

    @Override
    public Optional<PaymentEvent> findByMpPaymentId(Long mpPaymentId) {
        return jpaRepository.findByMpPaymentId(mpPaymentId).map(this::toDomain);
    }

    @Override
    public Optional<PaymentEvent> findByExternalReference(String externalReference) {
        return jpaRepository.findByExternalReference(externalReference).map(this::toDomain);
    }

    @Override
    public List<PaymentEvent> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<PaymentEvent> findByUserIdAndStatus(Long userId, PaymentStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updateStatus(Long id, PaymentStatus status, Long mpPaymentId, String errorDetail) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setMpPaymentId(mpPaymentId);
            entity.setErrorDetail(errorDetail);
            entity.setUpdatedAt(Instant.now());
            jpaRepository.save(entity);
        });
    }

    @Override
    public boolean approveIfPending(Long id, Long mpPaymentId) {
        int updated = jpaRepository.approveIfNotApproved(id, mpPaymentId, Instant.now(), PaymentStatus.APPROVED, PaymentStatus.PENDING);
        return updated > 0;
    }

    private PaymentEvent toDomain(PaymentEventEntity e) {
        return PaymentEvent.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .productId(e.getProductId())
                .externalReference(e.getExternalReference())
                .mpPreferenceId(e.getMpPreferenceId())
                .mpPaymentId(e.getMpPaymentId())
                .status(e.getStatus())
                .coinsAmount(e.getCoinsAmount())
                .productType(e.getProductType())
                .errorDetail(e.getErrorDetail())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private PaymentEventEntity toEntity(PaymentEvent e) {
        return PaymentEventEntity.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .productId(e.getProductId())
                .externalReference(e.getExternalReference())
                .mpPreferenceId(e.getMpPreferenceId())
                .mpPaymentId(e.getMpPaymentId())
                .status(e.getStatus())
                .coinsAmount(e.getCoinsAmount())
                .productType(e.getProductType())
                .errorDetail(e.getErrorDetail())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
