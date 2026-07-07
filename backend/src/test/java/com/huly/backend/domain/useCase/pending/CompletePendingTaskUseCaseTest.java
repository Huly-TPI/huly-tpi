package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.CompletePendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletePendingTaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    private CompletePendingTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CompletePendingTaskUseCase(pendingTaskRepository, new PendingTaskMapper());
    }

    @Test
    @DisplayName("Completa una tarea pendiente de manera exitosa")
    void executeShouldCompleteTask() {
        PendingTask task = pendingTask(PendingStatus.PENDING);
        PendingTask completed = pendingTask(PendingStatus.COMPLETED);
        givenTaskExists(1L, 10L, task);
        givenTaskMarkedCompleted(completed);

        PendingTaskResponse response = executeUseCase(1L, 10L);

        thenStatusIsCompleted(response);
    }

    @Test
    @DisplayName("Lanza excepción cuando la tarea a completar no existe")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Lanza excepción cuando la tarea ya se encuentra completada")
    void executeShouldThrowWhenTaskAlreadyCompleted() {
        PendingTask completed = pendingTask(PendingStatus.COMPLETED);
        givenTaskExists(1L, 10L, completed);

        assertThatThrownBy(() -> executeUseCase(1L, 10L))
                .isInstanceOf(IllegalStateException.class);
    }

    // --- arrange ---

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void FluxJunction_givenTaskMarkedCompleted(PendingTask completedTask) {}

    private void givenTaskMarkedCompleted(PendingTask completedTask) {
        when(pendingTaskRepository.markCompleted(any(), any())).thenReturn(completedTask);
    }

    // --- act ---

    private PendingTaskResponse executeUseCase(Long id, Long userId) {
        return useCase.execute(new CompletePendingTaskRequest(id, userId));
    }

    // --- assert ---

    private void thenStatusIsCompleted(PendingTaskResponse response) {
        assertThat(response.status()).isEqualTo(PendingStatus.COMPLETED);
    }

    // --- helpers ---

    private PendingTask pendingTask(PendingStatus status) {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Tarea")
                .status(status)
                .completedAt(status == PendingStatus.COMPLETED ? now() : null)
                .subtasks(List.of())
                .build();
    }
}
