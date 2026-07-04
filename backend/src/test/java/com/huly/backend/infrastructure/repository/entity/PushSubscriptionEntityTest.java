package com.huly.backend.infrastructure.repository.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PushSubscriptionEntityTest {

    @Test
    void builder_getters_and_onCreate_shouldWork() {
        PushSubscriptionEntity entity = PushSubscriptionEntity.builder()
                .id(1L).userId(5L).endpoint("ep").p256dh("k").auth("a").notificationHour(20).build();
        entity.onCreate();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUserId()).isEqualTo(5L);
        assertThat(entity.getEndpoint()).isEqualTo("ep");
        assertThat(entity.getP256dh()).isEqualTo("k");
        assertThat(entity.getAuth()).isEqualTo("a");
        assertThat(entity.getNotificationHour()).isEqualTo(20);
        assertThat(entity.getCreatedAt()).isNotNull();
    }
}