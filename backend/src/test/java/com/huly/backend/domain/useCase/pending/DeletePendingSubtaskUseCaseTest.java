package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.DeletePendingSubtaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePendingSubtaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingSubtaskRepository pendingSubtaskRepository;

    @Mock
    private PendingMentalLoadRefreshService mentalLoadRefreshService;

    private DeletePendingSubtaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePendingSubtaskUseCase(
                pendingTaskRepository,
                pendingSubtaskRepository,
                mentalLoadRefreshService
        );
    }

    @Test
    @DisplayName("Elimina una subtarea con éxito y refresca la carga mental de la tarea padre")
    void executeShouldDeleteSubtaskAndRefreshMentalLoad() {
        PendingTask task = pendingTask();
        PendingSubtask subtask = subtask();
        givenTaskExists(1L, 10L, task);
        givenSubtaskExists(100L, 1L, subtask);

        executeUseCase(1L, 100L, 10L);

        thenSubtaskDeleted(100L);
        thenMentalLoadRefreshed(task);
    }

    @Test
    @DisplayName("Lanza excepción cuando la tarea padre no existe al eliminar una subtarea")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 100L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Lanza excepción cuando la subtarea a eliminar no existe")
    void executeShouldThrowNotFoundWhenSubtaskDoesNotExist() {
        PendingTask task = pendingTask();
        givenTaskExists(1L, 10L, task);
        givenSubtaskDoesNotExist(100L, 1L);

        assertThatThrownBy(() -> executeUseCase(1L, 100L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- arrange ---

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void FluxJunction_givenTaskDoesNotExist(Long id, Long userId) {}

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void givenSubtaskExists(Long subtaskId, Long taskId, PendingSubtask subtask) {
        when(pendingSubtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.of(subtask));
    }

    private void givenSubtaskDoesNotExist(Long subtaskId, Long taskId) {
        when(pendingSubtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.empty());
    }

    // --- act ---

    private void executeUseCase(Long taskId, Long subtaskId, Long userId) {
        useCase.execute(new DeletePendingSubtaskRequest(taskId, subtaskId, userId));
    }

    // --- assert ---

    private void thenSubtaskDeleted(Long subtaskId) {
        verify(pendingSubtaskRepository).delete(subtaskId);
    }

    private void thenMentalLoadRefreshed(PendingTask task) {
        verify(mentalLoadRefreshService).refresh(task);
    }

    // --- helpers ---

    private PendingTask pendingTask() {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Tarea")
                .build();
    }

    private PendingSubtask subtask() {
        return PendingSubtask.builder()
                .id(100L)
                .taskId(1L)
                .text("Subtarea")
                .build();
    }
}
