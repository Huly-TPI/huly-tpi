package com.huly.backend.domain.service.pending;

import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Devuelve una lista vacía de recomendaciones si el usuario tiene menos de dos tareas pendientes")
    void recommendShouldReturnEmptyWhenFewerThanTwoPendingTasks() {
        List<PendingTask> tasks = List.of(task(1L, 0.5, MentalLoadBucket.MEDIUM, null));

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, NEUTRAL_TODAY);

        thenRecommendedTasksIsEmpty(result);
    }

    @Test
    @DisplayName("Incluye al menos una tarea de carga mental baja en la recomendación si hay alguna disponible")
    void recommendShouldIncludeAtLeastOneLowLoadTaskWhenAvailable() {
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.8, MentalLoadBucket.HIGH, null),
                task(3L, 0.8, MentalLoadBucket.HIGH, null)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, NEUTRAL_TODAY);

        thenRecommendedTasksContains(result, 1L);
    }

    @Test
    @DisplayName("Limita a un máximo de una tarea de carga mental alta por día en la recomendación diaria")
    void recommendShouldCapHighLoadTasksAtOnePerDay() {
        List<PendingTask> tasks = List.of(
                task(1L, 0.8, MentalLoadBucket.HIGH, null),
                task(2L, 0.8, MentalLoadBucket.HIGH, null),
                task(3L, 0.2, MentalLoadBucket.LOW, null)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 3.0, NEUTRAL_TODAY);

        thenHighLoadTaskCountIsLessThanOrEqualTo(tasks, result, 1);
    }

    @Test
    @DisplayName("No excede el presupuesto diario máximo de carga mental excepto por el mecanismo de red de seguridad")
    void recommendShouldNeverExceedBudgetExceptSafetyNetCase() {
        List<PendingTask> tasks = List.of(
                task(1L, 0.5, MentalLoadBucket.MEDIUM, null),
                task(2L, 0.5, MentalLoadBucket.MEDIUM, null),
                task(3L, 0.5, MentalLoadBucket.MEDIUM, null)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.0, NEUTRAL_TODAY);

        thenTotalLoadUsedIsLessThanOrEqualTo(result, 1.0);
    }

    @Test
    @DisplayName("Selecciona una sola tarea cuando todas las pendientes exceden individualmente el presupuesto de carga")
    void recommendShouldSelectSingleTaskWhenAllTasksExceedBudgetAlone() {
        List<PendingTask> tasks = List.of(
                task(1L, 5.0, MentalLoadBucket.HIGH, null),
                task(2L, 6.0, MentalLoadBucket.HIGH, null)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.0, NEUTRAL_TODAY);

        thenRecommendedTasksSizeIs(result, 1);
    }

    @Test
    @DisplayName("Prioriza la tarea de carga alta que vence antes cuando dos tareas altas compiten por el único espacio libre")
    void recommendShouldPreferTheHighTaskDueSoonerWhenTwoHighTasksCompeteForTheSingleSlot() {
        LocalDate today = LocalDate.of(2026, 7, 5);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.2, MentalLoadBucket.LOW, null),
                task(3L, 0.9, MentalLoadBucket.HIGH, null),
                task(4L, 0.9, MentalLoadBucket.HIGH, today)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, today);

        thenRecommendedTasksContains(result, 4L);
        thenRecommendedTasksDoesNotContain(result, 3L);
    }

    @Test
    @DisplayName("Sólo incluye la tarea alta que vence hoy y descarta la que vence mañana debido al límite diario")
    void recommendShouldOnlyIncludeTodaysHighTaskWhenAnotherHighIsDueTomorrow() {
        LocalDate today = LocalDate.of(2026, 7, 6);
        LocalDate tomorrow = today.plusDays(1);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.2, MentalLoadBucket.LOW, null),
                task(3L, 0.9, MentalLoadBucket.HIGH, today),
                task(4L, 0.9, MentalLoadBucket.HIGH, tomorrow)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, today);

        thenRecommendedTasksContains(result, 3L);
        thenRecommendedTasksDoesNotContain(result, 4L);
    }

    @Test
    @DisplayName("Incluye todas las tareas de carga alta que vencen hoy superando el límite diario para evitar atrasos")
    void recommendShouldIncludeAllHighTasksDueTodayWhenMultipleTieOnTheSameDate() {
        LocalDate today = LocalDate.of(2026, 7, 6);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.9, MentalLoadBucket.HIGH, today),
                task(3L, 1.0, MentalLoadBucket.HIGH, today),
                task(4L, 0.9, MentalLoadBucket.HIGH, null)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 3.0, today);

        thenRecommendedTasksContains(result, 2L, 3L);
        thenRecommendedTasksDoesNotContain(result, 4L);
    }

    @Test
    @DisplayName("Incluye una tarea de carga alta que vence dentro de los próximos 3 días aunque el presupuesto diario ya esté agotado")
    void recommendShouldForceIncludeTaskDueWithinNextThreeDaysEvenWhenBudgetIsExhausted() {
        LocalDate today = LocalDate.of(2026, 7, 9);
        LocalDate tomorrow = today.plusDays(1);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.5, MentalLoadBucket.MEDIUM, null),
                task(3L, 1.0, MentalLoadBucket.HIGH, tomorrow)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, today);

        thenRecommendedTasksContains(result, 3L);
    }

    @Test
    @DisplayName("No incluye por vencimiento próximo una tarea de carga alta perdedora del límite diario de una por día")
    void recommendShouldNotForceIncludeADueSoonTaskExcludedByTheDailyHighCap() {
        LocalDate today = LocalDate.of(2026, 7, 6);
        LocalDate tomorrow = today.plusDays(1);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.2, MentalLoadBucket.LOW, null),
                task(3L, 0.9, MentalLoadBucket.HIGH, today),
                task(4L, 0.9, MentalLoadBucket.HIGH, tomorrow)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, today);

        thenRecommendedTasksDoesNotContain(result, 4L);
    }

    @Test
    @DisplayName("No fuerza la inclusión de una tarea cuyo vencimiento está a más de 3 días")
    void recommendShouldNotForceIncludeATaskDueBeyondTheThreeDayHorizon() {
        LocalDate today = LocalDate.of(2026, 7, 9);
        LocalDate farAway = today.plusDays(4);
        List<PendingTask> tasks = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, null),
                task(2L, 0.5, MentalLoadBucket.MEDIUM, null),
                task(3L, 1.0, MentalLoadBucket.HIGH, farAway)
        );

        TaskBalanceRecommendationResult result = recommend(tasks, 1.6, today);

        thenRecommendedTasksDoesNotContain(result, 3L);
    }

    @Test
    @DisplayName("Genera la misma recomendación de forma determinista sin importar el orden de los elementos en la lista de entrada")
    void recommendShouldBeDeterministicRegardlessOfInputOrder() {
        List<PendingTask> ordered = List.of(
                task(1L, 0.2, MentalLoadBucket.LOW, LocalDate.of(2026, 1, 5)),
                task(2L, 0.5, MentalLoadBucket.MEDIUM, LocalDate.of(2026, 1, 1)),
                task(3L, 0.8, MentalLoadBucket.HIGH, null),
                task(4L, 0.3, MentalLoadBucket.LOW, null)
        );
        List<PendingTask> shuffled = new ArrayList<>(ordered);
        Collections.reverse(shuffled);

        TaskBalanceRecommendationResult first = recommend(ordered, 1.6, NEUTRAL_TODAY);
        TaskBalanceRecommendationResult second = recommend(shuffled, 1.6, NEUTRAL_TODAY);

        thenRecommendedTaskIdsAreEqual(first, second);
    }

    // --- act ---

    private TaskBalanceRecommendationResult recommend(List<PendingTask> tasks, double budget, LocalDate date) {
        return service.recommend(tasks, budget, date);
    }

    // --- assert ---

    private void thenRecommendedTasksIsEmpty(TaskBalanceRecommendationResult result) {
        assertThat(result.recommendedTaskIds()).isEmpty();
    }

    private void thenRecommendedTasksContains(TaskBalanceRecommendationResult result, Long... expectedIds) {
        assertThat(result.recommendedTaskIds()).contains(expectedIds);
    }

    private void thenRecommendedTasksDoesNotContain(TaskBalanceRecommendationResult result, Long... excludedIds) {
        assertThat(result.recommendedTaskIds()).doesNotContain(excludedIds);
    }

    private void thenHighLoadTaskCountIsLessThanOrEqualTo(List<PendingTask> originalTasks, TaskBalanceRecommendationResult result, int maxCount) {
        long highCount = originalTasks.stream()
                .filter(t -> result.recommendedTaskIds().contains(t.getId()))
                .filter(t -> t.getMentalLoadBucket() == MentalLoadBucket.HIGH)
                .count();
        assertThat(highCount).isLessThanOrEqualTo(maxCount);
    }

    private void thenTotalLoadUsedIsLessThanOrEqualTo(TaskBalanceRecommendationResult result, double maxLoad) {
        assertThat(result.totalLoadUsed()).isLessThanOrEqualTo(maxLoad);
    }

    private void thenRecommendedTasksSizeIs(TaskBalanceRecommendationResult result, int expectedSize) {
        assertThat(result.recommendedTaskIds()).hasSize(expectedSize);
    }

    private void thenRecommendedTaskIdsAreEqual(TaskBalanceRecommendationResult first, TaskBalanceRecommendationResult second) {
        assertThat(first.recommendedTaskIds()).isEqualTo(second.recommendedTaskIds());
    }

    // --- helpers ---

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
