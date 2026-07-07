package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pending_daily_recommendation_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingDailyRecommendationTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private PendingDailyRecommendationEntity recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private PendingTaskEntity task;
}
