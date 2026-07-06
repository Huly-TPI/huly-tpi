package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.GetPendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingRecommendationRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
public class GetPendingTaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingRecommendationRepository pendingRecommendationRepository;
    private final PendingTaskMapper mapper;

    public PendingTaskResponse execute(GetPendingTaskRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));
        boolean recommended = pendingRecommendationRepository
                .findAcceptedTaskIds(request.userId(), LocalDate.now())
                .contains(task.getId());
        return mapper.toResponse(task, recommended);
    }
}
