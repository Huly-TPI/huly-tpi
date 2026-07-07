package com.huly.backend.domain.model.pending;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDailyRecommendation {
    private Long id;
    private Long userId;
    private LocalDate recommendationDate;
    private RecommendationResponseDecision decision;
    private String pendingSetHash;
    private double totalLoadBudget;
    private double totalLoadUsed;
    @Builder.Default
    private List<Long> recommendedTaskIds = new ArrayList<>();
    private Instant createdAt;
    private Instant decidedAt;

    public void accept(Instant now) {
        requirePending();
        this.decision = RecommendationResponseDecision.ACCEPTED;
        this.decidedAt = now;
    }

    public void reject(Instant now) {
        requirePending();
        this.decision = RecommendationResponseDecision.REJECTED;
        this.decidedAt = now;
    }

    private void requirePending() {
        if (decision != RecommendationResponseDecision.PENDING) {
            throw new IllegalStateException("La recomendación ya fue respondida");
        }
    }
}
