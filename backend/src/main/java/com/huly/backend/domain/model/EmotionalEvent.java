package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
public class EmotionalEvent {
    private Long id;
    private Long userId;
    private EmotionalEventSource source;
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
    private RecommendationDecision recommendationDecision;
    private Integer feedbackScore;
    private String feedbackText;
    private Instant createdAt;
    private Instant updatedAt;
}
