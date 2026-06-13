package com.huly.backend.domain.repository;
import com.huly.backend.domain.model.PushSubscription;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository {
    PushSubscription save(PushSubscription pushSubscription);
    void deleteByEndpoint(String endpoint);
    boolean existsByEndpoint(String endpoint);
    List<PushSubscription> findAll();
    Optional<PushSubscription> findByUserId(Long userId);   
}
