package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.ActivityCorrelationStats;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.huly.backend.domain.model.enums.RecommendationDecision.ACCEPTED;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;

@RequiredArgsConstructor
public class GetActivityCorrelationUseCase {

    private final ActivityRepository activityRepository;
    private final EmotionalEventRepository emotionalEventRepository;

    public List<ActivityCorrelationStats> execute(Timeframe timeframe) {
        Instant startTime = Timeframe.getStartInstantFor(timeframe);
        List<Activity> activities = activityRepository.findAll();
        List<EmotionalEvent> events = emotionalEventRepository.findAllRecommendationEventsAfter(startTime);

        Map<Long, Map<String, List<EmotionalEvent>>> groupedEvents = groupEventsByActivityAndEmotion(events);

        return buildCorrelationStatsList(activities, groupedEvents);
    }

    private Map<Long, Map<String, List<EmotionalEvent>>> groupEventsByActivityAndEmotion(List<EmotionalEvent> events) {
        return events.stream()
                .filter(e -> e.getRecommendedActivityId() != null && e.getDetectedEmotion() != null)
                .collect(groupingBy(
                        EmotionalEvent::getRecommendedActivityId,
                        groupingBy(e -> e.getDetectedEmotion().trim().toUpperCase())
                ));
    }

    private List<ActivityCorrelationStats> buildCorrelationStatsList(
            List<Activity> activities,
            Map<Long, Map<String, List<EmotionalEvent>>> groupedEvents
    ) {
        List<ActivityCorrelationStats> statsList = new ArrayList<>();
        for (Activity activity : activities) {
            Map<String, List<EmotionalEvent>> emotionGroups = groupedEvents.getOrDefault(activity.getId(), Map.of());
            statsList.addAll(calculateStatsForActivity(activity, emotionGroups));
        }
        return statsList;
    }

    private List<ActivityCorrelationStats> calculateStatsForActivity(
            Activity activity,
            Map<String, List<EmotionalEvent>> emotionGroups
    ) {
        List<ActivityCorrelationStats> list = new ArrayList<>();
        for (EmotionType emotionType : EmotionType.values()) {
            List<EmotionalEvent> groupEvents = emotionGroups.getOrDefault(emotionType.name(), emptyList());
            list.add(buildCorrelationStats(activity, emotionType, groupEvents));
        }
        return list;
    }

    private ActivityCorrelationStats buildCorrelationStats(
            Activity activity,
            EmotionType emotionType,
            List<EmotionalEvent> groupEvents
    ) {
        long count = groupEvents.size();
        double acceptanceRate = calculateAcceptanceRate(groupEvents, count);

        return ActivityCorrelationStats.builder()
                .activityType(activity.getType().name())
                .emotion(emotionType.getDescription())
                .suggestionsCount(count)
                .acceptanceRate(acceptanceRate)
                .build();
    }

    private double calculateAcceptanceRate(List<EmotionalEvent> groupEvents, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        long accepted = groupEvents.stream()
                .filter(e -> e.getRecommendationDecision() == ACCEPTED)
                .count();
        return Math.round(((accepted * 100.0) / totalCount) * 10.0) / 10.0;
    }
}
