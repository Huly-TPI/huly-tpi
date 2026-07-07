package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.AddPendingSubtaskRequest;
import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
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
    private final PendingTaskMapper mapper;

    public PendingSubtaskResponse execute(AddPendingSubtaskRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.taskId()));

        PendingSubtask subtask = createSubtask(task.getId(), request.text());
        refreshTaskMentalLoad(request.taskId(), request.userId(), task);

        return mapper.toResponse(subtask);
    }

    private PendingSubtask createSubtask(Long taskId, String text) {
        int nextPosition = pendingSubtaskRepository.countByTaskId(taskId);
        return pendingSubtaskRepository.create(taskId, text, nextPosition);
    }

    private void refreshTaskMentalLoad(Long taskId, Long userId, PendingTask fallbackTask) {
        PendingTask refreshed = pendingTaskRepository.findByIdAndUserId(taskId, userId)
                .orElse(fallbackTask);
        mentalLoadRefreshService.refresh(refreshed);
    }
}
