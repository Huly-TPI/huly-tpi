package com.huly.backend.infrastructure.repository.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RiskWordEntityTest {

    private static final Instant PAST = Instant.parse("2020-01-01T00:00:00Z");

    private RiskWordEntity entity;

    @Test
    @DisplayName("onCreate asigna createdAt y updatedAt")
    void onCreateShouldSetTimestamps() {
        // --- arrange ---
        givenEntityWithoutTimestamps();

        // --- act ---
        callOnCreate();

        // --- assert ---
        thenTimestampsAreAssigned();
    }

    @Test
    @DisplayName("onUpdate actualiza updatedAt")
    void onUpdateShouldRefreshUpdatedAt() {
        // --- arrange ---
        givenEntityWithTimestamps();

        // --- act ---
        callOnUpdate();

        // --- assert ---
        thenUpdatedAtIsRefreshed();
    }

    // --- arrange ---

    private void givenEntityWithoutTimestamps() {
        entity = RiskWordEntity.builder().build();
    }

    private void givenEntityWithTimestamps() {
        entity = RiskWordEntity.builder()
                .createdAt(PAST)
                .updatedAt(PAST)
                .build();
    }

    // --- act ---

    private void callOnCreate() {
        entity.onCreate();
    }

    private void callOnUpdate() {
        entity.onUpdate();
    }

    // --- assert ---

    private void thenTimestampsAreAssigned() {
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isEqualTo(entity.getUpdatedAt());
    }

    private void thenUpdatedAtIsRefreshed() {
        assertThat(entity.getUpdatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isAfter(PAST);
    }
}
