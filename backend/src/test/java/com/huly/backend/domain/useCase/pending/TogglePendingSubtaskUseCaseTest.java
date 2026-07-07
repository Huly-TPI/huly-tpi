package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.TogglePendingSubtaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TogglePendingSubtaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingSubtaskRepository pendingSubtaskRepository;

    private TogglePendingSubtaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TogglePendingSubtaskUseCase(
                pendingTaskRepository,
                pendingSubtaskRepository,
                new PendingTaskMapper()
        );
    }

    @Test
    @DisplayName("Cambia el estado done de una subtarea con éxito")
    void executeShouldToggleSubtask() {
        PendingTask task = pendingTask();
        PendingSubtask subtask = subtask(100L, 1L, false);
        PendingSubtask toggled = subtask(100L, 1L, true);

        givenTaskExists(1L, 10L, task);
        givenSubtaskExists(100L, 1L, subtask);
        givenSubtaskToggled(100L, toggled);

        PendingSubtaskResponse response = executeUseCase(1L, 100L, 10L);

        thenSubtaskIsToggled(response, 100L, true);
        thenToggleCalled(100L);
    }

    @Test
    @DisplayName("Lanza excepción cuando la tarea padre no existe al alternar la subtarea")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 100L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Lanza excepción cuando la subtarea a alternar no existe")
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

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void givenSubtaskExists(Long subtaskId, Long taskId, PendingSubtask subtask) {
        when(pendingSubtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.of(subtask));
    }

    private void givenSubtaskDoesNotExist(Long subtaskId, Long taskId) {
        when(pendingSubtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.empty());
    }

    private void givenSubtaskToggled(Long subtaskId, PendingSubtask toggledSubtask) {
        when(pendingSubtaskRepository.toggle(subtaskId)).thenReturn(toggledSubtask);
    }

    // --- act ---

    private PendingSubtaskResponse executeUseCase(Long taskId, Long subtaskId, Long userId) {
        return useCase.execute(new TogglePendingSubtaskRequest(taskId, subtaskId, userId));
    }

    // --- assert ---

    private void thenSubtaskIsToggled(PendingSubtaskResponse response, Long id, boolean done) {
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.done()).isEqualTo(done);
    }

    private void thenToggleCalled(Long subtaskId) {
        verify(pendingSubtaskRepository).toggle(subtaskId);
    }

    // --- helpers ---

    private PendingTask pendingTask() {
        return PendingTask.builder().id(1L).userId(10L).build();
    }

    private PendingSubtask subtask(Long id, Long taskId, boolean done) {
        return PendingSubtask.builder().id(id).taskId(taskId).done(done).build();
    }
}
