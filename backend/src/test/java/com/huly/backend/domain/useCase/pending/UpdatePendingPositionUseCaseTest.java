package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pending.UpdatePendingPositionRequest;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
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
    @DisplayName("Asigna una rotación desde el servidor cuando es el primer pin de la tarea")
    void executeShouldAssignServerRotationOnFirstPin() {
        PendingTask unplaced = unplacedTask();
        givenTaskExists(1L, 10L, unplaced);
        givenPositionUpdated(1L, 20.0, 30.0, 4.2, placedTask(20.0, 30.0, 4.2));

        PendingTaskResponse response = executeUseCase(1L, 10L, 20.0, 30.0);

        thenRotationIs(response, 4.2);
        thenPositionUpdateVerified(1L, 20.0, 30.0, 4.2);
    }

    @Test
    @DisplayName("Mantiene la rotación existente cuando la tarea ya había sido posicionada previamente")
    void executeShouldKeepExistingRotationOnRepin() {
        PendingTask placed = placedTask(5.0, 5.0, 1.0);
        givenTaskExists(1L, 10L, placed);

        ArgumentCaptor<Double> rotationCaptor = ArgumentCaptor.forClass(Double.class);
        givenPositionUpdatedWithCaptor(1L, 70.0, 80.0, rotationCaptor, placedTask(70.0, 80.0, 1.0));

        executeUseCase(1L, 10L, 70.0, 80.0);

        thenRotationPassedIs(rotationCaptor, null);
    }

    // --- arrange ---

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void givenPositionUpdated(Long id, double x, double y, Double rotation, PendingTask result) {
        when(pendingTaskRepository.updatePosition(eq(id), eq(x), eq(y), eq(rotation), any(Instant.class)))
                .thenReturn(result);
    }

    private void givenPositionUpdatedWithCaptor(Long id, double x, double y, ArgumentCaptor<Double> captor, PendingTask result) {
        when(pendingTaskRepository.updatePosition(eq(id), eq(x), eq(y), captor.capture(), any(Instant.class)))
                .thenReturn(result);
    }

    // --- act ---

    private PendingTaskResponse executeUseCase(Long id, Long userId, double x, double y) {
        return useCase.execute(new UpdatePendingPositionRequest(id, userId, x, y));
    }

    // --- assert ---

    private void thenRotationIs(PendingTaskResponse response, double expectedRotation) {
        assertThat(response.rotationDeg()).isEqualTo(expectedRotation);
    }

    private void thenPositionUpdateVerified(Long id, double x, double y, Double rotation) {
        verify(pendingTaskRepository).updatePosition(eq(id), eq(x), eq(y), eq(rotation), any());
    }

    private void thenRotationPassedIs(ArgumentCaptor<Double> captor, Double expectedRotation) {
        assertThat(captor.getValue()).isEqualTo(expectedRotation);
    }

    // --- helpers ---

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
                .pinnedAt(now())
                .subtasks(List.of())
                .build();
    }
}
