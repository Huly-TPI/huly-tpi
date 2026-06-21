package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.PushSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
public interface IPushSubscriptionJpaRepository extends JpaRepository<PushSubscriptionEntity, Long> {

    @Transactional
    void deleteByEndpoint(String endpoint);

    boolean existsByEndpoint(String endpoint);

    Optional<PushSubscriptionEntity> findByEndpoint(String endpoint);

    Optional<PushSubscriptionEntity> findByUserId(Long userId);
    
}
