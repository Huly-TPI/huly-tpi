package com.huly.backend.domain.useCase.admin.chatbot;

import com.huly.backend.domain.dto.admin.chatbot.EmotionalCategoryDto;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;

import java.util.*;
import java.util.stream.Collectors;

public class GetEmotionalCategoriesUseCase {

    private final EmotionalEventRepository emotionalEventRepository;

    private static final List<EmotionType> DEFAULT_EMOTIONS = List.of(
            EmotionType.STRESS,
            EmotionType.ANXIETY,
            EmotionType.SADNESS,
            EmotionType.FEAR,
            EmotionType.ANGER,
            EmotionType.LONELINESS
    );

    public GetEmotionalCategoriesUseCase(EmotionalEventRepository emotionalEventRepository) {
        this.emotionalEventRepository = emotionalEventRepository;
    }

    public List<EmotionalCategoryDto> execute() {
        List<EmotionalEvent> events = emotionalEventRepository.findAll();
        return processEventsIntoCategories(events);
    }

    private List<EmotionalCategoryDto> processEventsIntoCategories(List<EmotionalEvent> events) {
        Map<String, List<EmotionalEvent>> eventsGroupedByEmotion = groupEventsByEmotion(events);
        List<EmotionalCategoryDto> categories = buildCategoryDtoList(eventsGroupedByEmotion);
        return fillMissingDefaultCategories(categories);
    }

    private Map<String, List<EmotionalEvent>> groupEventsByEmotion(List<EmotionalEvent> events) {
        return events.stream()
                .filter(event -> event.getDetectedEmotion() != null)
                .collect(Collectors.groupingBy(event -> event.getDetectedEmotion().toUpperCase()));
    }

    private List<EmotionalCategoryDto> buildCategoryDtoList(Map<String, List<EmotionalEvent>> groupedEvents) {
        List<EmotionalCategoryDto> list = new ArrayList<>();
        for (Map.Entry<String, List<EmotionalEvent>> entry : groupedEvents.entrySet()) {
            String emotionKey = entry.getKey();
            List<EmotionalEvent> emotionEvents = entry.getValue();

            String displayName = getEmotionDisplayName(emotionKey);
            int count = emotionEvents.size();
            int detectionPercentage = calculateAverageConfidencePercentage(emotionEvents);
            String severity = determineSeverity(emotionKey, emotionEvents);

            list.add(new EmotionalCategoryDto(displayName, count, detectionPercentage, severity));
        }
        return list;
    }

    private int calculateAverageConfidencePercentage(List<EmotionalEvent> events) {
        double averageConfidence = events.stream()
                .mapToDouble(event -> event.getConfidence() != null ? event.getConfidence() : 0.0)
                .average()
                .orElse(0.0);
        return scaleConfidenceToPercentage(averageConfidence);
    }

    private int scaleConfidenceToPercentage(double confidence) {
        return (int) Math.round(confidence <= 1.0 ? confidence * 100.0 : confidence);
    }

    private List<EmotionalCategoryDto> fillMissingDefaultCategories(List<EmotionalCategoryDto> existingCategories) {
        List<EmotionalCategoryDto> resultList = new ArrayList<>(existingCategories);
        Set<String> processedDisplayNames = getProcessedDisplayNames(existingCategories);

        for (EmotionType def : DEFAULT_EMOTIONS) {
            String displayName = cleanDescription(def.getDescription());
            if (!processedDisplayNames.contains(displayName)) {
                resultList.add(new EmotionalCategoryDto(displayName, 0, 0, determineSeverity(def.name(), Collections.emptyList())));
            }
        }
        return resultList;
    }

    private Set<String> getProcessedDisplayNames(List<EmotionalCategoryDto> categories) {
        return categories.stream()
                .map(EmotionalCategoryDto::name)
                .collect(Collectors.toSet());
    }

    private String getEmotionDisplayName(String emotionKey) {
        try {
            EmotionType type = EmotionType.valueOf(emotionKey.toUpperCase());
            String description = type.getDescription();
            return cleanDescription(description);
        } catch (IllegalArgumentException e) {
            return emotionKey;
        }
    }

    private String cleanDescription(String description) {
        if (description.contains("/")) {
            return description.split("/")[0].trim();
        }
        return description;
    }

    private String determineSeverity(String emotionKey, List<EmotionalEvent> events) {
        EmotionType type = parseEmotionType(emotionKey);
        double val = getAverageValence(events).orElse(type.getDefaultValence());
        double aro = getAverageArousal(events).orElse(type.getDefaultArousal());
        return calculateSeverityFromVad(val, aro);
    }

    private EmotionType parseEmotionType(String emotionKey) {
        try {
            return EmotionType.valueOf(emotionKey.toUpperCase());
        } catch (IllegalArgumentException e) {
            return EmotionType.NEUTRAL;
        }
    }

    private OptionalDouble getAverageValence(List<EmotionalEvent> events) {
        return events.stream()
                .filter(e -> e.getValence() != null)
                .mapToDouble(EmotionalEvent::getValence)
                .average();
    }

    private OptionalDouble getAverageArousal(List<EmotionalEvent> events) {
        return events.stream()
                .filter(e -> e.getArousal() != null)
                .mapToDouble(EmotionalEvent::getArousal)
                .average();
    }

    private String calculateSeverityFromVad(double valence, double arousal) {
        if (valence >= 0.0)
            return "BAJA";

        double distressScore = -valence * 0.6 + arousal * 0.4;

        if (distressScore >= 0.68)
            return "ALTA";
        else if (distressScore >= 0.25)
            return "MEDIA";
        else
            return "BAJA";
    }
}
