package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.ListPendingTasksRequest;
import com.huly.backend.domain.dto.pending.ListPendingTasksResponse;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
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
        List<PendingTask> tasks = pendingTaskRepository.findAllByUserId(request.userId(), request.statusFilter());
        Set<Long> recommendedTaskIds = pendingRecommendationRepository.findAcceptedTaskIds(request.userId(), LocalDate.now());
        return new ListPendingTasksResponse(
                tasks.stream().map(task -> mapper.toResponse(task, recommendedTaskIds)).toList()
        );
    }
}
