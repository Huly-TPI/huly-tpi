package com.huly.backend.infrastructure.repository.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EmotionalEventEntityTest {

    private static final Instant PAST = Instant.parse("2020-01-01T00:00:00Z");

    private EmotionalEventEntity entity;

    @Test
    @DisplayName("prePersist asigna createdAt y updatedAt cuando son nulos")
    void prePersistShouldSetTimestampsWhenNull() {
        // --- arrange ---
        givenEntityWithoutTimestamps();

        // --- act ---
        callPrePersist();

        // --- assert ---
        thenTimestampsAreAssigned();
    }

    @Test
    @DisplayName("prePersist conserva createdAt y updatedAt cuando ya están seteados")
    void prePersistShouldKeepExistingTimestamps() {
        // --- arrange ---
        givenEntityWithTimestamps();

        // --- act ---
        callPrePersist();

        // --- assert ---
        thenExistingTimestampsArePreserved();
    }

    @Test
    @DisplayName("preUpdate actualiza updatedAt")
    void preUpdateShouldRefreshUpdatedAt() {
        // --- arrange ---
        givenEntityWithTimestamps();

        // --- act ---
        callPreUpdate();

        // --- assert ---
        thenUpdatedAtIsRefreshed();
    }

    // --- arrange ---

    private void givenEntityWithoutTimestamps() {
        entity = EmotionalEventEntity.builder().build();
    }

    private void givenEntityWithTimestamps() {
        entity = EmotionalEventEntity.builder()
                .createdAt(PAST)
                .updatedAt(PAST)
                .build();
    }

    // --- act ---

    private void callPrePersist() {
        entity.prePersist();
    }

    private void callPreUpdate() {
        entity.preUpdate();
    }

    // --- assert ---

    private void thenTimestampsAreAssigned() {
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    private void thenExistingTimestampsArePreserved() {
        assertThat(entity.getCreatedAt()).isEqualTo(PAST);
        assertThat(entity.getUpdatedAt()).isEqualTo(PAST);
    }

    private void thenUpdatedAtIsRefreshed() {
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isAfter(PAST);
    }
}
