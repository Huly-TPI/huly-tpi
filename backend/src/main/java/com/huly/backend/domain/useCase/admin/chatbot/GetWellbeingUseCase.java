package com.huly.backend.domain.useCase.admin.chatbot;

import com.huly.backend.domain.dto.admin.chatbot.WellbeingDto;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetWellbeingUseCase {

    private final EmotionalEventRepository emotionalEventRepository;

    private static final Map<DayOfWeek, String> DAY_LABELS = Map.of(
            DayOfWeek.MONDAY, "Lun",
            DayOfWeek.TUESDAY, "Mar",
            DayOfWeek.WEDNESDAY, "Mié",
            DayOfWeek.THURSDAY, "Jue",
            DayOfWeek.FRIDAY, "Vie",
            DayOfWeek.SATURDAY, "Sáb",
            DayOfWeek.SUNDAY, "Dom"
    );

    public GetWellbeingUseCase(EmotionalEventRepository emotionalEventRepository) {
        this.emotionalEventRepository = emotionalEventRepository;
    }

    public WellbeingDto execute() {
        List<EmotionalEvent> events = emotionalEventRepository.findAll();
        return buildWeeklyWellbeing(events);
    }

    private WellbeingDto buildWeeklyWellbeing(List<EmotionalEvent> events) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        List<Integer> points = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            DayOfWeek day = targetDate.getDayOfWeek();
            labels.add(DAY_LABELS.getOrDefault(day, day.name().substring(0, 3)));

            List<EmotionalEvent> dayEvents = filterEventsByDate(events, targetDate, zone);
            int score = calculateDailyScore(dayEvents);
            points.add(score);
        }

        return new WellbeingDto(points, labels);
    }

    private List<EmotionalEvent> filterEventsByDate(List<EmotionalEvent> events, LocalDate date, ZoneId zone) {
        return events.stream()
                .filter(event -> event.getCreatedAt() != null &&
                        event.getCreatedAt().atZone(zone).toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    private int calculateDailyScore(List<EmotionalEvent> dayEvents) {
        if (dayEvents.isEmpty())
            return 0;

        return computeAverageValenceScore(dayEvents);
    }

    private int computeAverageValenceScore(List<EmotionalEvent> dayEvents) {
        double averageValence = dayEvents.stream()
                .mapToDouble(event -> event.getValence() != null ? event.getValence() : 0.0)
                .average()
                .orElse(0.0);
        return mapValenceToPercentageScale(averageValence);
    }

    private int mapValenceToPercentageScale(double valence) {
        int score = (int) Math.round((valence + 1.0) / 2.0 * 100.0);
        return Math.max(0, Math.min(100, score));
    }
}
