package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pending.UpdatePendingPositionRequest;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePendingPositionUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    private UpdatePendingPositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePendingPositionUseCase(pendingTaskRepository, new PendingTaskMapper(), () -> 4.2);
    }

    @Test
    void execute_shouldAssignServerRotation_onFirstPin() {
        PendingTask unplaced = unplacedTask();
        when(pendingTaskRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(unplaced));
        when(pendingTaskRepository.updatePosition(eq(1L), eq(20.0), eq(30.0), eq(4.2), any(Instant.class)))
                .thenReturn(placedTask(20.0, 30.0, 4.2));

        PendingTaskResponse response = useCase.execute(new UpdatePendingPositionRequest(1L, 10L, 20.0, 30.0));

        assertThat(response.rotationDeg()).isEqualTo(4.2);
        verify(pendingTaskRepository).updatePosition(eq(1L), eq(20.0), eq(30.0), eq(4.2), any());
    }

    @Test
    void execute_shouldKeepExistingRotation_onRepin() {
        PendingTask placed = placedTask(5.0, 5.0, 1.0);
        when(pendingTaskRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(placed));
        ArgumentCaptor<Double> rotationCaptor = ArgumentCaptor.forClass(Double.class);
        when(pendingTaskRepository.updatePosition(eq(1L), eq(70.0), eq(80.0), rotationCaptor.capture(), any(Instant.class)))
                .thenReturn(placedTask(70.0, 80.0, 1.0));

        useCase.execute(new UpdatePendingPositionRequest(1L, 10L, 70.0, 80.0));

        assertThat(rotationCaptor.getValue()).isNull();
    }

    private PendingTask unplacedTask() {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Tarea")
                .status(PendingStatus.PENDING)
                .subtasks(List.of())
                .build();
    }

    private PendingTask placedTask(double x, double y, double rotation) {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Tarea")
                .status(PendingStatus.PENDING)
                .positionX(x)
                .positionY(y)
                .rotationDeg(rotation)
                .pinnedAt(Instant.now())
                .subtasks(List.of())
                .build();
    }
}
