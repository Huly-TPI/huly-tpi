package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.ListPendingTasksRequest;
import com.huly.backend.domain.dto.pending.ListPendingTasksResponse;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPendingTasksUseCaseTest {

    @Mock
    private PendingTaskRepository pendingTaskRepository;

    @Mock
    private PendingRecommendationRepository pendingRecommendationRepository;

    private ListPendingTasksUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListPendingTasksUseCase(
                pendingTaskRepository,
                pendingRecommendationRepository,
                new PendingTaskMapper()
        );
    }

    @Test
    @DisplayName("Lista las tareas pendientes mapeando los flags de recomendación correctamente")
    void executeShouldListTasksAndFlagRecommendedCorrectly() {
        PendingTask task1 = pendingTask(1L, "Tarea 1");
        PendingTask task2 = pendingTask(2L, "Tarea 2");
        givenTasksReturned(10L, PendingStatus.PENDING, List.of(task1, task2));
        givenAcceptedRecommendations(10L, Set.of(1L));

        ListPendingTasksResponse response = executeUseCase(10L, PendingStatus.PENDING);

        thenTasksSizeIs(response, 2);
        thenTaskIsRecommendedAtIndex(response, 0, 1L, true);
        thenTaskIsRecommendedAtIndex(response, 1, 2L, false);
    }

    // --- arrange ---

    private void givenTasksReturned(Long userId, PendingStatus status, List<PendingTask> tasks) {
        when(pendingTaskRepository.findAllByUserId(userId, status)).thenReturn(tasks);
    }

    private void givenAcceptedRecommendations(Long userId, Set<Long> acceptedIds) {
        when(pendingRecommendationRepository.findAcceptedTaskIds(eq(userId), any(LocalDate.class)))
                .thenReturn(acceptedIds);
    }

    // --- act ---

    private ListPendingTasksResponse executeUseCase(Long userId, PendingStatus status) {
        return useCase.execute(new ListPendingTasksRequest(userId, status));
    }

    // --- assert ---

    private void thenTasksSizeIs(ListPendingTasksResponse response, int size) {
        assertThat(response.tasks()).hasSize(size);
    }

    private void thenTaskIsRecommendedAtIndex(ListPendingTasksResponse response, int index, Long id, boolean recommended) {
        assertThat(response.tasks().get(index).id()).isEqualTo(id);
        assertThat(response.tasks().get(index).recommended()).isEqualTo(recommended);
    }

    // --- helpers ---

    private PendingTask pendingTask(Long id, String title) {
        return PendingTask.builder().id(id).userId(10L).title(title).status(PendingStatus.PENDING).build();
    }
}
