package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.DeletePendingSubtaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePendingSubtaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingSubtaskRepository pendingSubtaskRepository;
    private final PendingMentalLoadRefreshService mentalLoadRefreshService;

    public void execute(DeletePendingSubtaskRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.taskId()));
        pendingSubtaskRepository.findByIdAndTaskId(request.subtaskId(), request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea", "id", request.subtaskId()));

        pendingSubtaskRepository.delete(request.subtaskId());

        PendingTask refreshed = pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId()).orElse(task);
        mentalLoadRefreshService.refresh(refreshed);
    }
}
