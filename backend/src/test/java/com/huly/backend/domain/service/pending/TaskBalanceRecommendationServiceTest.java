package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskBalanceRecommendationServiceTest {

    private static final LocalDate NEUTRAL_TODAY = LocalDate.of(2026, 1, 10);

    private final TaskBalanceRecommendationService service = new TaskBalanceRecommendationService();

    @Test
    void recommend_shouldReturnEmpty_whenFewerThanTwoPendingTasks() {
        TaskBalanceRecommendationResult result = service.recommend(List.of(task(1L, 0.5, MentalLoadBucket.MEDIUM, null)), 1.6, NEUTRAL_TODAY);

        assertThat(result.recommendedTaskIds()).isEmpty();
    }

    @Test
    void recommend_shouldIncludeAtLeastOneLowLoadTask_whenAvailable() {
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.8, MentalLoadBucket.HIGH, null),
                task(3L, 0.8, MentalLoadBucket.HIGH, null)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 1.6, NEUTRAL_TODAY);

        assertThat(result.recommendedTaskIds()).contains(1L);
    }

    @Test
    void recommend_shouldCapHighLoadTasksAtOnePerDay() {
        List<PendingTask> tasks = List.of(
                task(1L, 0.8, MentalLoadBucket.HIGH, null),
                task(2L, 0.8, MentalLoadBucket.HIGH, null),
                task(3L, 0.2, MentalLoadBucket.LOW, null)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 3.0, NEUTRAL_TODAY);

        long highCount = tasks.stream()
                .filter(t -> result.recommendedTaskIds().contains(t.getId()))
                .filter(t -> t.getMentalLoadBucket() == MentalLoadBucket.HIGH)
                .count();
        assertThat(highCount).isLessThanOrEqualTo(1);
    }

    @Test
    void recommend_shouldNeverExceedBudget_exceptSafetyNetCase() {
        List<PendingTask> tasks = List.of(
                task(1L, 0.5, MentalLoadBucket.MEDIUM, null),
                task(2L, 0.5, MentalLoadBucket.MEDIUM, null),
                task(3L, 0.5, MentalLoadBucket.MEDIUM, null)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 1.0, NEUTRAL_TODAY);

        assertThat(result.totalLoadUsed()).isLessThanOrEqualTo(1.0);
    }

    @Test
    void recommend_shouldSelectSingleTask_whenAllTasksExceedBudgetAlone() {
        List<PendingTask> tasks = List.of(
                task(1L, 5.0, MentalLoadBucket.HIGH, null),
                task(2L, 6.0, MentalLoadBucket.HIGH, null)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 1.0, NEUTRAL_TODAY);

        assertThat(result.recommendedTaskIds()).hasSize(1);
    }

    @Test
    void recommend_shouldPreferTheHighTaskDueSooner_whenTwoHighTasksCompeteForTheSingleSlot() {
        LocalDate today = LocalDate.of(2026, 7, 5);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.2, MentalLoadBucket.LOW, null),
                task(3L, 0.9, MentalLoadBucket.HIGH, null),
                task(4L, 0.9, MentalLoadBucket.HIGH, today)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 1.6, today);

        assertThat(result.recommendedTaskIds()).contains(4L);
        assertThat(result.recommendedTaskIds()).doesNotContain(3L);
    }

    @Test
    void recommend_shouldOnlyIncludeTodaysHighTask_whenAnotherHighIsDueTomorrow() {
        LocalDate today = LocalDate.of(2026, 7, 6);
        LocalDate tomorrow = today.plusDays(1);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.2, MentalLoadBucket.LOW, null),
                task(3L, 0.9, MentalLoadBucket.HIGH, today),
                task(4L, 0.9, MentalLoadBucket.HIGH, tomorrow)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 1.6, today);

        assertThat(result.recommendedTaskIds()).contains(3L);
        assertThat(result.recommendedTaskIds()).doesNotContain(4L);
    }

    @Test
    void recommend_shouldIncludeAllHighTasksDueToday_whenMultipleTieOnTheSameDate() {
        LocalDate today = LocalDate.of(2026, 7, 6);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.9, MentalLoadBucket.HIGH, today),
                task(3L, 1.0, MentalLoadBucket.HIGH, today),
                task(4L, 0.9, MentalLoadBucket.HIGH, null)
        );

        TaskBalanceRecommendationResult result = service.recommend(tasks, 3.0, today);

        assertThat(result.recommendedTaskIds()).contains(2L, 3L);
        assertThat(result.recommendedTaskIds()).doesNotContain(4L);
    }

    @Test
    void recommend_shouldBeDeterministic_regardlessOfInputOrder() {
        List<PendingTask> ordered = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, LocalDate.of(2026, 1, 5)),
                task(2L, 0.5, MentalLoadBucket.MEDIUM, LocalDate.of(2026, 1, 1)),
                task(3L, 0.8, MentalLoadBucket.HIGH, null),
                task(4L, 0.3, MentalLoadBucket.LOW, null)
        );
        List<PendingTask> shuffled = new ArrayList<>(ordered);
        Collections.reverse(shuffled);

        TaskBalanceRecommendationResult first = service.recommend(ordered, 1.6, NEUTRAL_TODAY);
        TaskBalanceRecommendationResult second = service.recommend(shuffled, 1.6, NEUTRAL_TODAY);

        assertThat(first.recommendedTaskIds()).isEqualTo(second.recommendedTaskIds());
    }

    private PendingTask task(Long id, double score, MentalLoadBucket bucket, LocalDate dueDate) {
        return PendingTask.builder()
                .id(id)
                .userId(1L)
                .title("Tarea " + id)
                .status(PendingStatus.PENDING)
                .mentalLoadScore(score)
                .mentalLoadBucket(bucket)
                .dueDate(dueDate)
                .build();
    }
}
