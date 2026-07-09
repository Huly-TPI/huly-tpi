package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pending.UpdatePendingTaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePendingTaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingMentalLoadRefreshService mentalLoadRefreshService;

    private UpdatePendingTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePendingTaskUseCase(
                pendingTaskRepository,
                mentalLoadRefreshService,
                new PendingTaskMapper()
        );
    }

    @Test
    @DisplayName("Modifica los campos editables de la tarea y actualiza su carga mental")
    void executeShouldUpdateFieldsAndRefreshMentalLoad() {
        PendingTask task = pendingTask(1L, "Original");
        PendingTask updated = pendingTask(1L, "Updated");

        givenTaskExists(1L, 10L, task);
        givenTaskUpdated(1L, "Updated", updated);
        givenMentalLoadRefreshed(updated, updated);

        PendingTaskResponse response = executeUseCase(1L, 10L, "Updated");

        thenTaskIsUpdated(response, 1L, "Updated");
        thenMentalLoadRefreshCalled(updated);
    }

    @Test
    @DisplayName("Lanza excepción cuando la tarea a actualizar no existe")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 10L, "Updated"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- arrange ---

    private void FluxJunction_givenTaskExists(Long id, Long userId, PendingTask task) {}

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void givenTaskUpdated(Long id, String title, PendingTask updatedTask) {
        when(pendingTaskRepository.updateFields(eq(id), eq(title), any(), any(), any(), any()))
                .thenReturn(updatedTask);
    }

    private void givenMentalLoadRefreshed(PendingTask task, PendingTask refreshed) {
        when(mentalLoadRefreshService.refresh(task)).thenReturn(refreshed);
    }

    // --- act ---

    private PendingTaskResponse executeUseCase(Long id, Long userId, String title) {
        return useCase.execute(new UpdatePendingTaskRequest(id, userId, title, null, null, null, null));
    }

    // --- assert ---

    private void thenTaskIsUpdated(PendingTaskResponse response, Long id, String title) {
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo(title);
    }

    private void thenMentalLoadRefreshCalled(PendingTask task) {
        verify(mentalLoadRefreshService).refresh(task);
    }

    // --- helpers ---

    private PendingTask pendingTask(Long id, String title) {
        return PendingTask.builder().id(id).userId(10L).title(title).status(PendingStatus.PENDING).subtasks(List.of()).build();
    }
}
