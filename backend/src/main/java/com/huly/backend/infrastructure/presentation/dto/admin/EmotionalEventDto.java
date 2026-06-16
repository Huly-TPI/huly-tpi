package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class EmotionalEventDto {
    private Long id;
    private String source;
    private String inputText;
    private String detectedEmotion;
    private Double confidence;
    private Double valence;
    private Double arousal;
    private Double dominance;
    private Double intensity;
    private String userGoal;
    private String generatedRecommendation;
    private Long recommendedActivityId;
    private Long chosenActivityId;
    private String recommendationDecision;
    private Integer feedbackScore;
    private String feedbackText;
    private Instant createdAt;
}
