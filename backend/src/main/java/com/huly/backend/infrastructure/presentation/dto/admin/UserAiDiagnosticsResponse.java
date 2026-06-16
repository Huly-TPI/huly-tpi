package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class UserAiDiagnosticsResponse {
    private List<VectorMemoryDto> aiMemories;
    private List<EmotionalEventDto> emotionalEvents;
    private String preferredName;
    private String communicationStyle;
    private String personalitySummary;
    private List<String> topicsDetected;
    private List<String> copingStrategies;
    private Integer receptivityScore;
    private String receptivityLabel;
    private List<String> acceptedActivities;
    private List<String> ignoredActivities;
    private String dominantEmotion;
    private Map<String, Integer> emotionDistribution;
}
