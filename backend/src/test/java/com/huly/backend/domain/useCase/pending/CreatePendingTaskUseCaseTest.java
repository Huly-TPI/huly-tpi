package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.CreatePendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationPort;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePendingTaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private MentalLoadEstimationPort mentalLoadEstimationPort;

    private CreatePendingTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        PendingMentalLoadRefreshService refreshService =
                new PendingMentalLoadRefreshService(mentalLoadEstimationPort, pendingTaskRepository);
        useCase = new CreatePendingTaskUseCase(pendingTaskRepository, refreshService, new PendingTaskMapper());
    }

    @Test
    @DisplayName("Crea una tarea pendiente y estima su carga mental")
    void executeShouldCreateTaskAndPersistMentalLoad() {
        PendingTask created = pendingTask(1L, "Lavar platos");
        PendingTask updated = withMentalLoad(created, 0.4, MentalLoadBucket.MEDIUM);
        givenTaskCreated(1L, "Lavar platos", created);
        givenMentalLoadEstimated(0.4, MentalLoadBucket.MEDIUM);
        givenMentalLoadUpdated(1L, 0.4, MentalLoadBucket.MEDIUM, updated);

        PendingTaskResponse response = executeUseCase(1L, "Lavar platos");

        thenTaskIsCreatedCorrectly(response, 1L, "Lavar platos");
        thenMentalLoadUpdated(1L, 0.4, MentalLoadBucket.MEDIUM);
    }

    @Test
    @DisplayName("Crea la tarea con éxito incluso si falla el puerto de estimación de carga mental")
    void executeShouldStillSucceedWhenMentalLoadPortThrows() {
        PendingTask created = pendingTask(2L, "Tarea");
        givenTaskCreated(1L, "Tarea", created);
        givenMentalLoadEstimationThrows(new RuntimeException("AI unavailable"));

        PendingTaskResponse response = executeUseCase(1L, "Tarea");

        thenTaskIsNotNull(response);
        thenTaskIdIs(response, 2L);
    }

    // --- arrange ---

    private void givenTaskCreated(Long userId, String title, PendingTask task) {
        when(pendingTaskRepository.create(eq(userId), eq(title), any(), any(), any(), any(), any()))
                .thenReturn(task);
    }

    private void givenMentalLoadEstimated(double score, MentalLoadBucket bucket) {
        when(mentalLoadEstimationPort.estimate(any())).thenReturn(new MentalLoadEstimate(score, bucket));
    }

    private void givenMentalLoadUpdated(Long id, double score, MentalLoadBucket bucket, PendingTask task) {
        when(pendingTaskRepository.updateMentalLoad(eq(id), anyDouble(), eq(bucket)))
                .thenReturn(task);
    }

    private void givenMentalLoadEstimationThrows(Throwable t) {
        when(mentalLoadEstimationPort.estimate(any())).thenThrow(t);
    }

    // --- act ---

    private PendingTaskResponse executeUseCase(Long userId, String title) {
        return useCase.execute(new CreatePendingTaskRequest(
                userId, title, null, null, null, null, List.of()));
    }

    // --- assert ---

    private void thenTaskIsCreatedCorrectly(PendingTaskResponse response, Long id, String title) {
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo(title);
    }

    private void thenMentalLoadUpdated(Long id, double score, MentalLoadBucket bucket) {
        verify(pendingTaskRepository).updateMentalLoad(id, score, bucket);
    }

    private void thenTaskIsNotNull(PendingTaskResponse response) {
        assertThat(response).isNotNull();
    }

    private void thenTaskIdIs(PendingTaskResponse response, Long id) {
        assertThat(response.id()).isEqualTo(id);
    }

    // --- helpers ---

    private PendingTask pendingTask(Long id, String title) {
        return PendingTask.builder()
                .id(id)
                .userId(1L)
                .title(title)
                .status(PendingStatus.PENDING)
                .subtasks(List.of())
                .build();
    }

    private PendingTask withMentalLoad(PendingTask task, double score, MentalLoadBucket bucket) {
        task.setMentalLoadScore(score);
        task.setMentalLoadBucket(bucket);
        return task;
    }
}
