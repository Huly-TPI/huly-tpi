package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pending.GetPendingTaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPendingTaskUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingRecommendationRepository pendingRecommendationRepository;

    private GetPendingTaskUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPendingTaskUseCase(
                pendingTaskRepository,
                pendingRecommendationRepository,
                new PendingTaskMapper()
        );
    }

    @Test
    @DisplayName("Obtiene una tarea existente y la marca como recomendada si fue aceptada para hoy")
    void executeShouldGetTaskMarkedRecommended() {
        PendingTask task = pendingTask();
        givenTaskExists(1L, 10L, task);
        givenAcceptedRecommendations(10L, Set.of(1L));

        PendingTaskResponse response = executeUseCase(1L, 10L);

        thenTaskIdIs(response, 1L);
        thenTaskIsRecommended(response, true);
    }

    @Test
    @DisplayName("Obtiene una tarea existente y la marca como no recomendada si no fue aceptada para hoy")
    void executeShouldGetTaskNotRecommended() {
        PendingTask task = pendingTask();
        givenTaskExists(1L, 10L, task);
        givenAcceptedRecommendations(10L, Collections.emptySet());

        PendingTaskResponse response = executeUseCase(1L, 10L);

        thenTaskIdIs(response, 1L);
        thenTaskIsRecommended(response, false);
    }

    @Test
    @DisplayName("Lanza excepción si la tarea consultada no existe")
    void executeShouldThrowNotFoundWhenTaskDoesNotExist() {
        givenTaskDoesNotExist(1L, 10L);

        assertThatThrownBy(() -> executeUseCase(1L, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- arrange ---

    private void givenTaskExists(Long id, Long userId, PendingTask task) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(task));
    }

    private void givenTaskDoesNotExist(Long id, Long userId) {
        when(pendingTaskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());
    }

    private void givenAcceptedRecommendations(Long userId, Set<Long> acceptedIds) {
        when(pendingRecommendationRepository.findAcceptedTaskIds(eq(userId), any(LocalDate.class)))
                .thenReturn(acceptedIds);
    }

    // --- act ---

    private PendingTaskResponse executeUseCase(Long id, Long userId) {
        return useCase.execute(new GetPendingTaskRequest(id, userId));
    }

    // --- assert ---

    private void thenTaskIdIs(PendingTaskResponse response, Long id) {
        assertThat(response.id()).isEqualTo(id);
    }

    private void thenTaskIsRecommended(PendingTaskResponse response, boolean expected) {
        assertThat(response.recommended()).isEqualTo(expected);
    }

    // --- helpers ---

    private PendingTask pendingTask() {
        return PendingTask.builder()
                .id(1L)
                .userId(10L)
                .title("Tarea")
                .status(PendingStatus.PENDING)
                .subtasks(List.of())
                .build();
    }
}
