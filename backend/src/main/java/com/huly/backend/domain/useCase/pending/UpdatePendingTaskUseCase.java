package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pending.UpdatePendingTaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdatePendingTaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingMentalLoadRefreshService mentalLoadRefreshService;
    private final PendingTaskMapper mapper;

    public PendingTaskResponse execute(UpdatePendingTaskRequest request) {
        pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));

        PendingTask updated = updateFields(request);
        PendingTask withMentalLoad = refreshMentalLoad(updated);
        return mapper.toResponse(withMentalLoad, false);
    }

    private PendingTask updateFields(UpdatePendingTaskRequest request) {
        return pendingTaskRepository.updateFields(
                request.id(), request.title(), request.description(), request.dueDate(),
                request.estimatedDuration(), request.category());
    }

    private PendingTask refreshMentalLoad(PendingTask task) {
        return mentalLoadRefreshService.refresh(task);
    }
}
