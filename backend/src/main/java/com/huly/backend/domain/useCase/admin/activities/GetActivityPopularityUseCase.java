package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.ActivityPopularityStats;
import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@RequiredArgsConstructor
public class GetActivityPopularityUseCase {

    private final ActivityRepository activityRepository;
    private final ActivitySessionRepository activitySessionRepository;

    public List<ActivityPopularityStats> execute(Timeframe timeframe) {
        Instant startTime = Timeframe.getStartInstantFor(timeframe);
        List<Activity> activities = activityRepository.findAll();
        List<ActivitySession> sessions = activitySessionRepository.findAllAfter(startTime);

        Map<ActivityType, Long> sessionCountsByActivity = groupAndCountSessionsByActivity(sessions);

        return buildPopularityStatsList(activities, sessionCountsByActivity);
    }

    private Map<ActivityType, Long> groupAndCountSessionsByActivity(List<ActivitySession> sessions) {
        return sessions.stream()
                .collect(groupingBy(ActivitySession::getActivityType, counting()));
    }

    private List<ActivityPopularityStats> buildPopularityStatsList(
            List<Activity> activities,
            Map<ActivityType, Long> sessionCounts
    ) {
        List<ActivityPopularityStats> statsList = new ArrayList<>();
        for (Activity activity : activities) {
            long count = sessionCounts.getOrDefault(activity.getType(), 0L);
            statsList.add(buildPopularityStats(activity, count));
        }
        return statsList;
    }

    private ActivityPopularityStats buildPopularityStats(Activity activity, long count) {
        return ActivityPopularityStats.builder()
                .activityType(activity.getType().name())
                .activityName(activity.getTitle())
                .totalSessions(count)
                .build();
    }
}
