package com.huly.backend.domain.model.emotionalRecommendation;

import com.huly.backend.domain.model.enums.EmotionalEventSource;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

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

    public boolean isUnhelpful(int index, List<EmotionalEvent> userHistory) {
        boolean hasLowFeedback = this.feedbackScore != null && this.feedbackScore <= 2;
        return hasLowFeedback || hasNoEmotionalImprovement(index, userHistory);
    }

    public boolean hasNoEmotionalImprovement(int index, List<EmotionalEvent> userHistory) {
        if (userHistory == null || index <= 0 || index >= userHistory.size())
            return false;

        EmotionalEvent nextEvent = userHistory.get(index - 1);
        if (nextEvent == null || nextEvent.getValence() == null || this.valence == null)
            return false;

        double valenceDelta = nextEvent.getValence() - this.valence;
        return valenceDelta <= 0.0;
    }
}
