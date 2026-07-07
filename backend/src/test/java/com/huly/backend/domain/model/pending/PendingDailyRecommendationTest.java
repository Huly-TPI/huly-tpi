package com.huly.backend.domain.model.pending;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PendingDailyRecommendationTest {

    @Test
    @DisplayName("Establece la decisión en ACCEPTED y la marca de tiempo de la decisión al aceptar")
    void acceptShouldSetDecisionAndDecidedAt() {
        PendingDailyRecommendation recommendation = pendingRecommendation();
        Instant now = Instant.now();

        performAccept(recommendation, now);

        thenDecisionIs(recommendation, RecommendationResponseDecision.ACCEPTED);
        thenDecidedAtIs(recommendation, now);
    }

    @Test
    @DisplayName("Establece la decisión en REJECTED y la marca de tiempo de la decisión al rechazar")
    void rejectShouldSetDecisionAndDecidedAt() {
        PendingDailyRecommendation recommendation = pendingRecommendation();
        Instant now = Instant.now();

        performReject(recommendation, now);

        thenDecisionIs(recommendation, RecommendationResponseDecision.REJECTED);
        thenDecidedAtIs(recommendation, now);
    }

    @Test
    @DisplayName("Lanza excepción si se intenta decidir sobre una recomendación que ya fue decidida previamente")
    void acceptShouldThrowWhenAlreadyDecided() {
        PendingDailyRecommendation recommendation = pendingRecommendation();
        givenAlreadyAccepted(recommendation);

        assertThatThrownBy(() -> performReject(recommendation, Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    // --- arrange ---

    private void givenAlreadyAccepted(PendingDailyRecommendation recommendation) {
        recommendation.accept(Instant.now());
    }

    // --- act ---

    private void performAccept(PendingDailyRecommendation recommendation, Instant now) {
        recommendation.accept(now);
    }

    private void performReject(PendingDailyRecommendation recommendation, Instant now) {
        recommendation.reject(now);
    }

    // --- assert ---

    private void thenDecisionIs(PendingDailyRecommendation recommendation, RecommendationResponseDecision expected) {
        assertThat(recommendation.getDecision()).isEqualTo(expected);
    }

    private void thenDecidedAtIs(PendingDailyRecommendation recommendation, Instant expected) {
        assertThat(recommendation.getDecidedAt()).isEqualTo(expected);
    }

    // --- helpers ---

    private PendingDailyRecommendation pendingRecommendation() {
        return PendingDailyRecommendation.builder()
                .id(1L)
                .userId(10L)
                .decision(RecommendationResponseDecision.PENDING)
                .build();
    }
}
