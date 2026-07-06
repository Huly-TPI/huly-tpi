package com.huly.backend.domain.model.pending;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingDailyRecommendationTest {

    @Test
    void accept_shouldSetDecisionAndDecidedAt() {
        PendingDailyRecommendation recommendation = pendingRecommendation();
        Instant now = Instant.now();

        recommendation.accept(now);

        assertThat(recommendation.getDecision()).isEqualTo(RecommendationResponseDecision.ACCEPTED);
        assertThat(recommendation.getDecidedAt()).isEqualTo(now);
    }

    @Test
    void reject_shouldSetDecisionAndDecidedAt() {
        PendingDailyRecommendation recommendation = pendingRecommendation();
        Instant now = Instant.now();

        recommendation.reject(now);

        assertThat(recommendation.getDecision()).isEqualTo(RecommendationResponseDecision.REJECTED);
        assertThat(recommendation.getDecidedAt()).isEqualTo(now);
    }

    @Test
    void accept_shouldThrow_whenAlreadyDecided() {
        PendingDailyRecommendation recommendation = pendingRecommendation();
        recommendation.accept(Instant.now());

        assertThatThrownBy(() -> recommendation.reject(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    private PendingDailyRecommendation pendingRecommendation() {
        return PendingDailyRecommendation.builder()
                .id(1L)
                .userId(10L)
                .decision(RecommendationResponseDecision.PENDING)
                .build();
    }
}
