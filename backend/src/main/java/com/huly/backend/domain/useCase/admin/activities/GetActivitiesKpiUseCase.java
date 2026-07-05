package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.ActivitiesKpiStats;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.huly.backend.domain.model.enums.RecommendationDecision.ACCEPTED;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class GetActivitiesKpiUseCase {

    private final ActivityRepository activityRepository;
    private final EmotionalEventRepository emotionalEventRepository;
    private final ActivitySessionRepository activitySessionRepository;

    public ActivitiesKpiStats execute(Timeframe timeframe) {
        Instant startTime = Timeframe.getStartInstantFor(timeframe);
        List<Activity> activities = activityRepository.findAll();
        List<ActivitySession> sessions = activitySessionRepository.findAllAfter(startTime);
        List<EmotionalEvent> events = emotionalEventRepository.findAllRecommendationEventsAfter(startTime);

        double avgMoodImprovement = calculateAverageMoodImprovement(activities, events);

        return buildKpiStats(sessions, avgMoodImprovement);
    }

    private double calculateAverageMoodImprovement(List<Activity> activities, List<EmotionalEvent> events) {
        Map<Long, List<EmotionalEvent>> userEventsMap = loadUserEventsMap(events);

        Map<Long, List<EmotionalEvent>> eventsByActivity = events.stream()
                .filter(e -> e.getRecommendedActivityId() != null)
                .collect(groupingBy(EmotionalEvent::getRecommendedActivityId));

        double moodImprovementSum = 0.0;
        int activitiesWithImprovementCount = 0;

        for (Activity activity : activities) {
            List<EmotionalEvent> activityEvents = eventsByActivity.getOrDefault(activity.getId(), emptyList());
            if (!activityEvents.isEmpty()) {
                Double improvementRate = calculateImprovementRate(activityEvents, userEventsMap);
                if (improvementRate != null) {
                    moodImprovementSum += improvementRate;
                    activitiesWithImprovementCount++;
                }
            }
        }

        return calculateFinalAverage(moodImprovementSum, activitiesWithImprovementCount);
    }

    private Map<Long, List<EmotionalEvent>> loadUserEventsMap(List<EmotionalEvent> events) {
        List<Long> userIds = events.stream()
                .map(EmotionalEvent::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return Map.of();
        }

        List<EmotionalEvent> allUserEvents = emotionalEventRepository.findByUserIds(userIds);
        return allUserEvents.stream()
                .collect(groupingBy(
                        EmotionalEvent::getUserId,
                        collectingAndThen(
                                toList(),
                                list -> {
                                    List<EmotionalEvent> sorted = new ArrayList<>(list);
                                    sorted.sort(Comparator.comparing(EmotionalEvent::getCreatedAt));
                                    return sorted;
                                }
                        )
                ));
    }

    private Double calculateImprovementRate(
            List<EmotionalEvent> activityEvents,
            Map<Long, List<EmotionalEvent>> userEventsMap
    ) {
        long acceptedWithNext = 0;
        long valenceImproved = 0;

        for (EmotionalEvent event : activityEvents) {
            if (event.getRecommendationDecision() == ACCEPTED) {
                List<EmotionalEvent> userEvents = userEventsMap.getOrDefault(event.getUserId(), emptyList());
                Optional<EmotionalEvent> nextEventOpt = findNextEvent(userEvents, event);

                if (nextEventOpt.isPresent()) {
                    acceptedWithNext++;
                    if (hasValenceImproved(event, nextEventOpt.get())) {
                        valenceImproved++;
                    }
                }
            }
        }
        return acceptedWithNext > 0 ? (valenceImproved * 100.0) / acceptedWithNext : null;
    }

    private Optional<EmotionalEvent> findNextEvent(List<EmotionalEvent> userEvents, EmotionalEvent currentEvent) {
        return userEvents.stream()
                .filter(next -> next.getCreatedAt().isAfter(currentEvent.getCreatedAt()))
                .findFirst();
    }

    private boolean hasValenceImproved(EmotionalEvent current, EmotionalEvent next) {
        if (next.getValence() != null && current.getValence() != null) {
            return (next.getValence() - current.getValence()) > 0;
        }
        return false;
    }

    private double calculateFinalAverage(double sum, int count) {
        return count > 0 ? Math.round((sum / count) * 10.0) / 10.0 : 0.0;
    }

    private ActivitiesKpiStats buildKpiStats(List<ActivitySession> sessions, double avgImprovement) {
        if (sessions.isEmpty()) {
            return ActivitiesKpiStats.builder()
                    .totalSessions(0)
                    .topActivityType(null)
                    .topActivitySessions(0)
                    .averageMoodImprovement(avgImprovement)
                    .build();
        }

        Map<ActivityType, Long> sessionCounts = sessions.stream()
                .collect(groupingBy(ActivitySession::getActivityType, counting()));

        Map.Entry<ActivityType, Long> topEntry = sessionCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        String topType = topEntry != null ? topEntry.getKey().name() : null;
        long topSessions = topEntry != null ? topEntry.getValue() : 0;

        return ActivitiesKpiStats.builder()
                .totalSessions(sessions.size())
                .topActivityType(topType)
                .topActivitySessions(topSessions)
                .averageMoodImprovement(avgImprovement)
                .build();
    }
}
