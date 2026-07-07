package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.RecommendationResponseDecision;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "pending_daily_recommendation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDailyRecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private RecommendationResponseDecision decision;

    @Column(name = "pending_set_hash", nullable = false, length = 64)
    private String pendingSetHash;

    @Column(name = "total_load_budget", nullable = false)
    private double totalLoadBudget;

    @Column(name = "total_load_used", nullable = false)
    private double totalLoadUsed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
