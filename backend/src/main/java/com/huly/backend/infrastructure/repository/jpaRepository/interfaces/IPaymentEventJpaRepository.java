package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.infrastructure.repository.entity.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface IPaymentEventJpaRepository extends JpaRepository<PaymentEventEntity, Long> {
    Optional<PaymentEventEntity> findByMpPaymentId(Long mpPaymentId);
    Optional<PaymentEventEntity> findByExternalReference(String externalReference);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PaymentEventEntity e SET e.status = :approved, e.mpPaymentId = :mpPaymentId, e.updatedAt = :updatedAt " +
           "WHERE e.id = :id AND e.status = :pending")
    int approveIfNotApproved(@Param("id") Long id,
                             @Param("mpPaymentId") Long mpPaymentId,
                             @Param("updatedAt") Instant updatedAt,
                             @Param("approved") PaymentStatus approved,
                             @Param("pending") PaymentStatus pending);
}
