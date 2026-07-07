package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.DeletePendingTaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
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
class DeletePendingTaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    private DeletePendingTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePendingTaskUseCase(pendingTaskRepository);
    }

    @Test
    @DisplayName("Elimina una tarea pendiente con éxito")
    void executeShouldDeleteTask() {
        PendingTask task = pendingTask(1L, 10L);
        givenTaskExists(1L, 10L, task);

        executeUseCase(1L, 10L);

        thenTaskDeleted(1L, 10L);
    }

    @Test
    @DisplayName("Lanza excepción si la tarea a eliminar no existe")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
        thenTaskNeverDeleted();
    }

    // --- arrange ---

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    // --- act ---

    private void executeUseCase(Long id, Long userId) {
        useCase.execute(new DeletePendingTaskRequest(id, userId));
    }

    // --- assert ---

    private void thenTaskDeleted(Long id, Long userId) {
        verify(pendingTaskRepository).delete(id, userId);
    }

    private void thenTaskNeverDeleted() {
        verify(pendingTaskRepository, never()).delete(anyLong(), anyLong());
    }

    // --- helpers ---

    private PendingTask pendingTask(Long id, Long userId) {
        return PendingTask.builder().id(id).userId(userId).build();
    }
}
