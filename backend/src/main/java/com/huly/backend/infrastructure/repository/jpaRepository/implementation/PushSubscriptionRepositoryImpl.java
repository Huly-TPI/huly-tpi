package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import com.huly.backend.infrastructure.repository.entity.PushSubscriptionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPushSubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PushSubscriptionRepositoryImpl implements PushSubscriptionRepository {

    private final IPushSubscriptionJpaRepository jpaRepository;

    @Override
    public PushSubscription save(PushSubscription pushSubscription) {
        PushSubscriptionEntity saved = jpaRepository.save(toEntity(pushSubscription));
        return toDomain(saved);
    }

    @Override
    public void deleteByEndpoint(String endpoint) {
        jpaRepository.deleteByEndpoint(endpoint);
    }

    @Override
    public boolean existsByEndpoint(String endpoint) {
        return jpaRepository.existsByEndpoint(endpoint);
    }

    @Override
    public List<PushSubscription> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<PushSubscription> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<PushSubscription> findByNotificationHour(int hour) {
        return jpaRepository.findByNotificationHour(hour).stream().map(this::toDomain).toList();
    }

    @Override
    public void updateNotificationHourByUserId(Long userId, int hour) {
        jpaRepository.updateNotificationHourByUserId(userId, hour);
    }

    private PushSubscriptionEntity toEntity(PushSubscription d) {
        return PushSubscriptionEntity.builder().id(d.getId()).userId(d.getUserId())
                .endpoint(d.getEndpoint()).p256dh(d.getP256dh()).auth(d.getAuth())
                .notificationHour(d.getNotificationHour())
                .build();
    }

    private PushSubscription toDomain(PushSubscriptionEntity e) {
        return PushSubscription.builder().id(e.getId()).userId(e.getUserId()).endpoint(e.getEndpoint())
                .p256dh(e.getP256dh()).auth(e.getAuth())
                .notificationHour(e.getNotificationHour())
                .build();
    }

}
