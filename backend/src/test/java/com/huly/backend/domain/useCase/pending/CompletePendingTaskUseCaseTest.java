package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.CompletePendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    void execute_shouldCompleteTask() {
        PendingTask task = pendingTask();
        when(pendingTaskRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(task));
        when(pendingTaskRepository.markCompleted(any(), any())).thenReturn(completedTask());

        PendingTaskResponse response = useCase.execute(new CompletePendingTaskRequest(1L, 10L));

        assertThat(response.status()).isEqualTo(PendingStatus.COMPLETED);
    }

    @Test
    void execute_shouldThrowNotFound_whenTaskDoesNotExist() {
        when(pendingTaskRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new CompletePendingTaskRequest(1L, 10L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_shouldThrow_whenTaskAlreadyCompleted() {
        when(pendingTaskRepository.findByIdAndUserId(1L, 10L)).thenReturn(Optional.of(completedTask()));

        assertThatThrownBy(() -> useCase.execute(new CompletePendingTaskRequest(1L, 10L)))
                .isInstanceOf(IllegalStateException.class);
    }

    private PendingTask pendingTask() {
        return PendingTask.builder()
                .id(1L).userId(10L).title("Tarea").status(PendingStatus.PENDING).subtasks(List.of()).build();
    }

    private PendingTask completedTask() {
        return PendingTask.builder()
                .id(1L).userId(10L).title("Tarea").status(PendingStatus.COMPLETED)
                .completedAt(Instant.now()).subtasks(List.of()).build();
    }
}
