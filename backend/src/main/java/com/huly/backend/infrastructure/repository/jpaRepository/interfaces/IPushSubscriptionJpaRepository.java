package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.PushSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.Optional;

public interface IPushSubscriptionJpaRepository extends JpaRepository<PushSubscriptionEntity, Long> {

    @Transactional
    void deleteByEndpoint(String endpoint);

    boolean existsByEndpoint(String endpoint);

    Optional<PushSubscriptionEntity> findByEndpoint(String endpoint);

    Optional<PushSubscriptionEntity> findByUserId(Long userId);

    List<PushSubscriptionEntity> findByNotificationHour(int notificationHour);

    @Modifying
    @Transactional
    @Query("UPDATE PushSubscriptionEntity p SET p.notificationHour = :hour WHERE p.userId = :userId")
    void updateNotificationHourByUserId(@Param("userId") Long userId, @Param("hour") int hour);

}
