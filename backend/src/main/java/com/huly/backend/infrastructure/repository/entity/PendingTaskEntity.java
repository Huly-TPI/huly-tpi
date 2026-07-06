package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pending_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_duration", length = 20)
    private EstimatedDuration estimatedDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private PendingCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PendingStatus status;

    @Column(name = "mental_load_score")
    private Double mentalLoadScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "mental_load_bucket", length = 10)
    private MentalLoadBucket mentalLoadBucket;

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "rotation_deg")
    private Double rotationDeg;

    @Column(name = "pinned_at")
    private Instant pinnedAt;

    @Builder.Default
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PendingSubtaskEntity> subtasks = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
