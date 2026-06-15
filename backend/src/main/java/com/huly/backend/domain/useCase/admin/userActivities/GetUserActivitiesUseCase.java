package com.huly.backend.domain.useCase.admin.userActivities;

import com.huly.backend.domain.model.ActivitySession;
import com.huly.backend.domain.repository.ActivitySessionRepository;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class GetUserActivitiesUseCase {

    private final UserRepository userRepository;
    private final ActivitySessionRepository activitySessionRepository;

    public GetUserActivitiesResponse execute(GetUserActivitiesRequest request) {
        Long userId = request.userId();
        String timeframe = request.timeframe();

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = now.atZone(zone).toLocalDate();
        Instant timeframeStart = resolveTimeframeStart(timeframe, now, zone, today);

        List<ActivitySession> filteredSessions = timeframeStart == null
                ? activitySessionRepository.findByUserId(userId)
                : activitySessionRepository.findByUserIdAndCreatedAtAfter(userId, timeframeStart);

        long todayActivitiesCount = "today".equalsIgnoreCase(timeframe)
                ? filteredSessions.size()
                : activitySessionRepository.countByUserIdAndCreatedAtAfter(userId, today.atStartOfDay(zone).toInstant());

        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("RESPIRACION", 0);
        distribution.put("DIARIO", 0);
        distribution.put("NUBE", 0);
        distribution.put("BURBUJA", 0);
        distribution.put("RETO", 0);

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
            if ("today".equalsIgnoreCase(timeframe)) {
                averageSessionsText = totalSessionsCount + (totalSessionsCount == 1 ? " sesión hoy" : " sesiones hoy");
            } else if ("week".equalsIgnoreCase(timeframe)) {
                averageSessionsText = totalSessionsCount + (totalSessionsCount == 1 ? " sesión/semana" : " sesiones/semana");
            } else {
                long oldestSessionEpochMillis = activitySessionRepository.findOldestSessionByUserId(userId)
                        .map(ActivitySession::getCreatedAt)
                        .filter(Objects::nonNull)
                        .map(Instant::toEpochMilli)
                        .orElseThrow(() -> new IllegalStateException("Missing oldest activity session for userId=" + userId));

                long diffMillis = now.toEpochMilli() - oldestSessionEpochMillis;
                double diffDays = Math.max(1.0, diffMillis / (1000.0 * 60.0 * 60.0 * 24.0));
                double timeframeDays = "month".equalsIgnoreCase(timeframe) ? 30.0 : diffDays;
                double activeDays = Math.min(timeframeDays, diffDays);
                double diffWeeks = Math.max(1.0, activeDays / 7.0);
                double averagePerWeek = Math.round((totalSessionsCount / diffWeeks) * 10.0) / 10.0;
                String averagePerWeekText = averagePerWeek % 1 == 0
                        ? String.valueOf((int) averagePerWeek)
                        : String.valueOf(averagePerWeek);
                averageSessionsText = averagePerWeekText + (averagePerWeek == 1.0 ? " sesión/semana" : " sesiones/semana");
            }
        }

        List<ActivitySession> recentSessions = timeframeStart == null
                ? activitySessionRepository.findRecentByUserId(userId, 5)
                : activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(userId, timeframeStart, 5);

        List<ActivitySessionResponse> activitySessions = recentSessions.stream()
                .map(session -> new ActivitySessionResponse(
                        session.getId(),
                        Objects.requireNonNull(session.getActivityType(), "ActivitySession activityType is required").name(),
                        session.getCreatedAt()
                ))
                .toList();

        return new GetUserActivitiesResponse(
                activitySessions,
                todayActivitiesCount,
                favoriteActivity,
                averageSessionsText,
                distribution
        );
    }

    private Instant resolveTimeframeStart(String timeframe, Instant now, ZoneId zone, LocalDate today) {
        if (timeframe == null || timeframe.equalsIgnoreCase("total")) {
            return null;
        }
        if (timeframe.equalsIgnoreCase("today")) {
            return today.atStartOfDay(zone).toInstant();
        }
        if (timeframe.equalsIgnoreCase("week")) {
            return now.minus(7, ChronoUnit.DAYS);
        }
        if (timeframe.equalsIgnoreCase("month")) {
            return now.minus(30, ChronoUnit.DAYS);
        }
        throw new IllegalArgumentException("Invalid timeframe: " + timeframe);
    }
}
