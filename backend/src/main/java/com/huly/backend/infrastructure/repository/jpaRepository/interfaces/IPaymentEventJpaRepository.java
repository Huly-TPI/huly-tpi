package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPaymentEventJpaRepository extends JpaRepository<PaymentEventEntity, Long> {
    Optional<PaymentEventEntity> findByMpPaymentId(Long mpPaymentId);
    Optional<PaymentEventEntity> findByMpPreferenceId(String mpPreferenceId);
}
