package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "emotional_event")
public class EmotionalEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private EmotionalEventSource source;

    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "detected_emotion", nullable = false, length = 100)
    private String detectedEmotion;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "valence")
    private Double valence;

    @Column(name = "arousal")
    private Double arousal;

    @Column(name = "dominance")
    private Double dominance;

    @Column(name = "intensity")
    private Double intensity;

    @Column(name = "user_goal")
    private String userGoal;

    @Column(name = "generated_recommendation", columnDefinition = "TEXT")
    private String generatedRecommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_activity_id")
    private ActivityEntity recommendedActivity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_activity_id")
    private ActivityEntity chosenActivity;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_decision", length = 50)
    private RecommendationDecision recommendationDecision;

    @Column(name = "feedback_score")
    private Integer feedbackScore;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
