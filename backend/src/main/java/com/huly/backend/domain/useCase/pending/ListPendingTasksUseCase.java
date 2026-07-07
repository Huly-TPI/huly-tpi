package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.ListPendingTasksRequest;
import com.huly.backend.domain.dto.pending.ListPendingTasksResponse;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.enums.PendingStatus;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ListPendingTasksUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingRecommendationRepository pendingRecommendationRepository;
    private final PendingTaskMapper mapper;

    public ListPendingTasksResponse execute(ListPendingTasksRequest request) {
        List<PendingTask> tasks = getTasksByUserIdAndStatus(request.userId(), request.statusFilter());
        Set<Long> recommendedTaskIds = getTodayRecommendedTaskIds(request.userId());
        return new ListPendingTasksResponse(mapper.toResponse(tasks, recommendedTaskIds));
    }

    private List<PendingTask> getTasksByUserIdAndStatus(Long userId, PendingStatus statusFilter) {
        return pendingTaskRepository.findAllByUserId(userId, statusFilter);
    }

    private Set<Long> getTodayRecommendedTaskIds(Long userId) {
        return pendingRecommendationRepository.findAcceptedTaskIds(userId, LocalDate.now());
    }
}
