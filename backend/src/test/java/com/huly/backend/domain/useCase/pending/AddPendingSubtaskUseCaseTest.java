package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.AddPendingSubtaskRequest;
import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddPendingSubtaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingSubtaskRepository pendingSubtaskRepository;

    @Mock
    private PendingMentalLoadRefreshService mentalLoadRefreshService;

    private AddPendingSubtaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AddPendingSubtaskUseCase(
                pendingTaskRepository,
                pendingSubtaskRepository,
                mentalLoadRefreshService,
                new PendingTaskMapper()
        );
    }

    @Test
    @DisplayName("Agrega una subtarea con éxito y refresca la carga mental de la tarea padre")
    void executeShouldAddSubtaskAndRefreshMentalLoad() {
        PendingTask task = pendingTask();
        PendingSubtask subtask = subtask();
        givenTaskExists(1L, 10L, task);
        givenSubtaskCount(1L, 0);
        givenSubtaskCreated(1L, "Nueva subtarea", 0, subtask);

        PendingSubtaskResponse response = executeUseCase(1L, 10L, "Nueva subtarea");

        thenSubtaskCreatedCorrectly(response, 100L, "Nueva subtarea");
        thenMentalLoadRefreshed(task);
    }

    @Test
    @DisplayName("Lanza excepción si la tarea padre no existe")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 10L, "Nueva subtarea"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- arrange ---

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void givenSubtaskCount(Long taskId, int count) {
        when(pendingSubtaskRepository.countByTaskId(taskId)).thenReturn(count);
    }

    private void givenSubtaskCreated(Long taskId, String text, int position, PendingSubtask subtask) {
        when(pendingSubtaskRepository.create(taskId, text, position)).thenReturn(subtask);
    }

    // --- act ---

    private PendingSubtaskResponse executeUseCase(Long taskId, Long userId, String text) {
        return useCase.execute(new AddPendingSubtaskRequest(taskId, userId, text));
    }

    // --- assert ---

    private void thenSubtaskCreatedCorrectly(PendingSubtaskResponse response, Long id, String text) {
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.text()).isEqualTo(text);
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
                .status(PendingStatus.PENDING)
                .subtasks(new ArrayList<>())
                .build();
    }

    private PendingSubtask subtask() {
        return PendingSubtask.builder()
                .id(100L)
                .taskId(1L)
                .text("Nueva subtarea")
                .done(false)
                .position(0)
                .build();
    }
}
