package com.huly.backend.domain.useCase.admin.userAiDiagnostics;

import java.util.List;
import java.util.Map;

public record GetUserAiDiagnosticsResponse(
        List<VectorMemoryResponse> aiMemories,
        List<EmotionalEventResponse> emotionalEvents,
        String preferredName,
        String communicationStyle,
        String personalitySummary,
        List<String> topicsDetected,
        List<String> copingStrategies,
        int receptivityScore,
        String receptivityLabel,
        List<String> acceptedActivities,
        List<String> ignoredActivities,
        String dominantEmotion,
        Map<String, Integer> emotionDistribution
) {
}
