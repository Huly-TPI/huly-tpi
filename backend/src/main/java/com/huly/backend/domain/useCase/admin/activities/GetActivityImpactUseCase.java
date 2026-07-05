package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.ActivityImpactStats;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class GetActivityImpactUseCase {

    private final ActivityRepository activityRepository;
    private final EmotionalEventRepository emotionalEventRepository;

    private record ImprovementDeltas(long acceptedWithNext, double totalValenceDelta, double totalArousalDelta) {}

    public List<ActivityImpactStats> execute(Timeframe timeframe) {
        Instant startTime = Timeframe.getStartInstantFor(timeframe);
        List<Activity> activities = activityRepository.findAll();
        List<EmotionalEvent> events = emotionalEventRepository.findAllRecommendationEventsAfter(startTime);

        Map<Long, List<EmotionalEvent>> userEventsMap = loadUserEventsMap(events);
        Map<Long, List<EmotionalEvent>> eventsByActivity = groupEventsByRecommendedActivity(events);

        return calculateActivitiesImpact(activities, eventsByActivity, userEventsMap);
    }

    private Map<Long, List<EmotionalEvent>> groupEventsByRecommendedActivity(List<EmotionalEvent> events) {
        return events.stream()
                .filter(e -> e.getRecommendedActivityId() != null)
                .collect(groupingBy(EmotionalEvent::getRecommendedActivityId));
    }

    private Map<Long, List<EmotionalEvent>> loadUserEventsMap(List<EmotionalEvent> events) {
        List<Long> userIds = events.stream()
                .map(EmotionalEvent::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (userIds.isEmpty())
            return Map.of();

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

    private List<ActivityImpactStats> calculateActivitiesImpact(
            List<Activity> activities,
            Map<Long, List<EmotionalEvent>> eventsByActivity,
            Map<Long, List<EmotionalEvent>> userEventsMap
    ) {
        List<ActivityImpactStats> statsList = new ArrayList<>();
        for (Activity activity : activities) {
            List<EmotionalEvent> activityEvents = eventsByActivity.getOrDefault(activity.getId(), emptyList());
            ImprovementDeltas deltas = calculateImprovementDeltas(activityEvents, userEventsMap);
            statsList.add(buildActivityImpactStats(activity, deltas));
        }
        return statsList;
    }

    private ImprovementDeltas calculateImprovementDeltas(
            List<EmotionalEvent> activityEvents,
            Map<Long, List<EmotionalEvent>> userEventsMap
    ) {
        long acceptedWithNext = 0;
        double totalValenceDelta = 0.0;
        double totalArousalDelta = 0.0;

        for (EmotionalEvent event : activityEvents) {
            List<EmotionalEvent> userEvents = userEventsMap.getOrDefault(event.getUserId(), emptyList());
            Optional<EmotionalEvent> nextEventOpt = findNextEvent(userEvents, event);

            if (nextEventOpt.isPresent()) {
                acceptedWithNext++;
                EmotionalEvent nextEvent = nextEventOpt.get();
                totalValenceDelta += calculateValenceDelta(event, nextEvent);
                totalArousalDelta += calculateArousalDelta(event, nextEvent);
            }
        }

        return new ImprovementDeltas(acceptedWithNext, totalValenceDelta, totalArousalDelta);
    }

    private Optional<EmotionalEvent> findNextEvent(List<EmotionalEvent> userEvents, EmotionalEvent currentEvent) {
        return userEvents.stream()
                .filter(next -> next.getCreatedAt().isAfter(currentEvent.getCreatedAt()))
                .findFirst();
    }

    private double calculateValenceDelta(EmotionalEvent current, EmotionalEvent next) {
        if (next.getValence() != null && current.getValence() != null) {
            return next.getValence() - current.getValence();
        }
        return 0.0;
    }

    private double calculateArousalDelta(EmotionalEvent current, EmotionalEvent next) {
        if (next.getArousal() != null && current.getArousal() != null) {
            return next.getArousal() - current.getArousal();
        }
        return 0.0;
    }

    private ActivityImpactStats buildActivityImpactStats(Activity activity, ImprovementDeltas deltas) {
        boolean basedOnMetrics = deltas.acceptedWithNext() > 0;
        double avgValence = basedOnMetrics
                ? roundMetric(deltas.totalValenceDelta() / deltas.acceptedWithNext())
                : activity.getEffectValence();

        double avgArousal = basedOnMetrics
                ? roundMetric(deltas.totalArousalDelta() / deltas.acceptedWithNext())
                : activity.getEffectArousal();

        return ActivityImpactStats.builder()
                .activityType(activity.getType().name())
                .averageValenceChange(avgValence)
                .averageArousalChange(avgArousal)
                .basedOnMetrics(basedOnMetrics)
                .build();
    }

    private double roundMetric(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
