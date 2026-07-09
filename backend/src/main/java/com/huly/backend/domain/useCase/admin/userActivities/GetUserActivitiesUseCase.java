package com.huly.backend.domain.useCase.admin.userActivities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class GetUserActivitiesUseCase {

    private final UserRepository userRepository;
    private final ActivitySessionRepository activitySessionRepository;
    private final ActivityRepository activityRepository;

    public GetUserActivitiesResponse execute(GetUserActivitiesRequest request) {
        Long userId = request.userId();
        Timeframe timeframe = request.timeframe();

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = now.atZone(zone).toLocalDate();
        Instant timeframeStart = timeframe.getStartInstant(now, zone);

        List<ActivitySession> filteredSessions = timeframeStart == null
                ? activitySessionRepository.findByUserId(userId)
                : activitySessionRepository.findByUserIdAndCreatedAtAfter(userId, timeframeStart);

        long todayActivitiesCount = timeframe == Timeframe.TODAY
                ? filteredSessions.size()
                : activitySessionRepository.countByUserIdAndCreatedAtAfter(userId, today.atStartOfDay(zone).toInstant());

        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("BREATHING", 0);
        distribution.put("DIARY", 0);
        distribution.put("LANTERN", 0);
        distribution.put("BUBBLE", 0);
        distribution.put("CHALLENGE", 0);
        distribution.put("ZEN_GARDEN", 0);
        distribution.put("MANDALA", 0);
        distribution.put("STONES", 0);
        distribution.put("PENDING", 0);

        for (ActivitySession session : filteredSessions) {
            String typeName = Objects.requireNonNull(session.getActivityType(), "ActivitySession activityType is required").name();
            if (distribution.containsKey(typeName)) {
                distribution.put(typeName, distribution.get(typeName) + 1);
            }
        }

        String favoriteActivity = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                favoriteActivity = entry.getKey();
            }
        }

        String averageSessionsText = "Sin registros";
        int totalSessionsCount = filteredSessions.size();
        if (totalSessionsCount > 0) {
            if (timeframe == Timeframe.TODAY) {
                averageSessionsText = totalSessionsCount + (totalSessionsCount == 1 ? " sesión hoy" : " sesiones hoy");
            } else if (timeframe == Timeframe.WEEK) {
                averageSessionsText = totalSessionsCount + (totalSessionsCount == 1 ? " sesión/semana" : " sesiones/semana");
            } else {
                long oldestSessionEpochMillis = activitySessionRepository.findOldestSessionByUserId(userId)
                        .map(ActivitySession::getCreatedAt)
                        .filter(Objects::nonNull)
                        .map(Instant::toEpochMilli)
                        .orElseThrow(() -> new IllegalStateException("Missing oldest activity session for userId=" + userId));

                long diffMillis = now.toEpochMilli() - oldestSessionEpochMillis;
                double diffDays = Math.max(1.0, diffMillis / (1000.0 * 60.0 * 60.0 * 24.0));
                double timeframeDays = timeframe == Timeframe.MONTH ? 30.0 : diffDays;
                double activeDays = Math.min(timeframeDays, diffDays);
                double diffWeeks = Math.max(1.0, activeDays / 7.0);
                double averagePerWeek = Math.round((totalSessionsCount / diffWeeks) * 10.0) / 10.0;
                String averagePerWeekText = averagePerWeek % 1 == 0
                        ? String.valueOf((int) averagePerWeek)
                        : String.valueOf(averagePerWeek);
                averageSessionsText = averagePerWeekText + (averagePerWeek == 1.0 ? " sesión/semana" : " sesiones/semana");
            }
        }

        Map<String, String> activityNamesMap = new HashMap<>();

        try {
            List<Activity> activitiesList = activityRepository.findAll();
            for (Activity act : activitiesList) {
                if (act.getType() != null && act.getTitle() != null) {
                    activityNamesMap.put(act.getType().name(), act.getTitle());
                }
            }
        } catch (Exception ignored) {
        }

        List<ActivitySession> recentSessions = timeframeStart == null
                ? activitySessionRepository.findRecentByUserId(userId, 5)
                : activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(userId, timeframeStart, 5);

        List<ActivitySessionResponse> activitySessions = recentSessions.stream()
                .map(session -> {
                    String typeName = Objects.requireNonNull(session.getActivityType(), "ActivitySession activityType is required").name();
                    String displayName = activityNamesMap.getOrDefault(typeName, typeName);
                    return new ActivitySessionResponse(
                            session.getId(),
                            typeName,
                            displayName,
                            session.getCreatedAt()
                    );
                })
                .toList();

        return new GetUserActivitiesResponse(
                activitySessions,
                todayActivitiesCount,
                favoriteActivity,
                averageSessionsText,
                distribution,
                activityNamesMap
        );
    }
}
