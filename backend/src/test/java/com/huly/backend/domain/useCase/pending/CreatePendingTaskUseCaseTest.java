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
    void execute_shouldCreateTaskAndPersistMentalLoad() {
        PendingTask created = pendingTask(1L);
        when(pendingTaskRepository.create(eq(1L), eq("Lavar platos"), any(), any(), any(), any(), any()))
                .thenReturn(created);
        when(mentalLoadEstimationPort.estimate(any())).thenReturn(new MentalLoadEstimate(0.4, MentalLoadBucket.MEDIUM));
        when(pendingTaskRepository.updateMentalLoad(eq(1L), anyDouble(), eq(MentalLoadBucket.MEDIUM)))
                .thenReturn(withMentalLoad(created, 0.4, MentalLoadBucket.MEDIUM));

        PendingTaskResponse response = useCase.execute(new CreatePendingTaskRequest(
                1L, "Lavar platos", null, null, null, null, List.of()));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Lavar platos");
        verify(pendingTaskRepository).updateMentalLoad(1L, 0.4, MentalLoadBucket.MEDIUM);
    }

    @Test
    void execute_shouldStillSucceed_whenMentalLoadPortThrows() {
        PendingTask created = pendingTask(2L);
        when(pendingTaskRepository.create(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenReturn(created);
        when(mentalLoadEstimationPort.estimate(any())).thenThrow(new RuntimeException("AI unavailable"));

        PendingTaskResponse response = useCase.execute(new CreatePendingTaskRequest(
                1L, "Tarea", null, null, null, null, List.of()));

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
    }

    private PendingTask pendingTask(Long id) {
        return PendingTask.builder()
                .id(id)
                .userId(1L)
                .title(id == 1L ? "Lavar platos" : "Tarea")
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
