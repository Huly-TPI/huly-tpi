package com.huly.backend.domain.mapper.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PendingTaskMapperTest {

    private PendingTaskMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PendingTaskMapper();
    }

    @Test
    @DisplayName("Mapea una tarea pendiente a su DTO de respuesta con flag de recomendada")
    void toResponseShouldMapPendingTaskWithRecommendedFlag() {
        PendingTask task = buildFullTask();

        PendingTaskResponse response = mapToResponse(task, true);

        thenTaskResponseMatches(response, true);
    }

    @Test
    @DisplayName("Mapea una tarea pendiente a su DTO de respuesta utilizando un set de IDs recomendados")
    void toResponseShouldMapPendingTaskWithRecommendedSet() {
        PendingTask task = buildEmptyTask();

        PendingTaskResponse response = mapToResponseWithSet(task, Set.of(1L));

        thenTaskIsRecommended(response, true);
        thenTaskHasNoSubtasks(response);
    }

    @Test
    @DisplayName("Mapea una subtarea a su DTO de respuesta")
    void toResponseShouldMapPendingSubtask() {
        PendingSubtask subtask = buildSubtask();

        PendingSubtaskResponse response = mapSubtaskToResponse(subtask);

        thenSubtaskResponseMatches(response);
    }

    @Test
    @DisplayName("Mapea una lista de tareas pendientes a su lista de DTOs correspondientes")
    void toResponseShouldMapTaskList() {
        PendingTask task1 = buildTaskWithId(1L);
        PendingTask task2 = buildTaskWithId(2L);

        List<PendingTaskResponse> responses = mapTaskListToResponse(List.of(task1, task2), Set.of(2L));

        thenListMatches(responses);
    }

    // --- act ---

    private PendingTaskResponse mapToResponse(PendingTask task, boolean recommended) {
        return mapper.toResponse(task, recommended);
    }

    private PendingTaskResponse mapToResponseWithSet(PendingTask task, Set<Long> recommendedSet) {
        return mapper.toResponse(task, recommendedSet);
    }

    private PendingSubtaskResponse mapSubtaskToResponse(PendingSubtask subtask) {
        return mapper.toResponse(subtask);
    }

    private List<PendingTaskResponse> mapTaskListToResponse(List<PendingTask> tasks, Set<Long> recommendedSet) {
        return mapper.toResponse(tasks, recommendedSet);
    }

    // --- assert ---

    private void thenTaskResponseMatches(PendingTaskResponse response, boolean recommended) {
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Tarea");
        assertThat(response.description()).isEqualTo("Desc");
        assertThat(response.estimatedDuration()).isEqualTo(EstimatedDuration.FIFTEEN_MIN);
        assertThat(response.category()).isEqualTo(PendingCategory.SALUD);
        assertThat(response.status()).isEqualTo(PendingStatus.PENDING);
        assertThat(response.positionX()).isEqualTo(12.5);
        assertThat(response.positionY()).isEqualTo(30.0);
        assertThat(response.rotationDeg()).isEqualTo(4.2);
        assertThat(response.recommended()).isEqualTo(recommended);
        assertThat(response.subtasks()).hasSize(1);
        assertThat(response.subtasks().get(0).id()).isEqualTo(100L);
    }

    private void thenTaskIsRecommended(PendingTaskResponse response, boolean recommended) {
        assertThat(response.recommended()).isEqualTo(recommended);
    }

    private void thenTaskHasNoSubtasks(PendingTaskResponse response) {
        assertThat(response.subtasks()).isEmpty();
    }

    private void thenSubtaskResponseMatches(PendingSubtaskResponse response) {
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.taskId()).isEqualTo(1L);
        assertThat(response.text()).isEqualTo("Subtarea");
        assertThat(response.done()).isTrue();
        assertThat(response.position()).isEqualTo(2);
    }

    private void thenListMatches(List<PendingTaskResponse> responses) {
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).recommended()).isFalse();
        assertThat(responses.get(1).recommended()).isTrue();
    }

    // --- helpers ---

    private PendingTask buildFullTask() {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Tarea")
                .description("Desc")
                .estimatedDuration(EstimatedDuration.FIFTEEN_MIN)
                .category(PendingCategory.SALUD)
                .status(PendingStatus.PENDING)
                .positionX(12.5)
                .positionY(30.0)
                .rotationDeg(4.2)
                .pinnedAt(Instant.now())
                .createdAt(Instant.now())
                .completedAt(null)
                .subtasks(List.of(
                        PendingSubtask.builder().id(100L).taskId(1L).text("Sub").done(true).position(1).build()
                ))
                .build();
    }

    private PendingTask buildEmptyTask() {
        return PendingTask.builder().id(1L).subtasks(null).build();
    }

    private PendingTask buildTaskWithId(Long id) {
        return PendingTask.builder().id(id).build();
    }

    private PendingSubtask buildSubtask() {
        return PendingSubtask.builder()
                .id(100L)
                .taskId(1L)
                .text("Subtarea")
                .done(true)
                .position(2)
                .build();
    }
}
