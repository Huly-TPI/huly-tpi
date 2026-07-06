package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.AddPendingSubtaskRequest;
import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import com.huly.backend.domain.service.pending.PendingMentalLoadRefreshService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddPendingSubtaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingSubtaskRepository pendingSubtaskRepository;
    private final PendingMentalLoadRefreshService mentalLoadRefreshService;

    public PendingSubtaskResponse execute(AddPendingSubtaskRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.taskId()));

        int nextPosition = pendingSubtaskRepository.countByTaskId(task.getId());
        PendingSubtask subtask = pendingSubtaskRepository.create(task.getId(), request.text(), nextPosition);

        PendingTask refreshed = pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId()).orElse(task);
        mentalLoadRefreshService.refresh(refreshed);

        return toResponse(subtask);
    }

    private PendingSubtaskResponse toResponse(PendingSubtask subtask) {
        return new PendingSubtaskResponse(subtask.getId(), subtask.getTaskId(), subtask.getText(), subtask.isDone(), subtask.getPosition());
    }
}
