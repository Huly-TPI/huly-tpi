package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.TogglePendingSubtaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TogglePendingSubtaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingSubtaskRepository pendingSubtaskRepository;

    public PendingSubtaskResponse execute(TogglePendingSubtaskRequest request) {
        pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.taskId()));
        pendingSubtaskRepository.findByIdAndTaskId(request.subtaskId(), request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea", "id", request.subtaskId()));

        PendingSubtask toggled = pendingSubtaskRepository.toggle(request.subtaskId());
        return new PendingSubtaskResponse(toggled.getId(), toggled.getTaskId(), toggled.getText(), toggled.isDone(), toggled.getPosition());
    }
}
