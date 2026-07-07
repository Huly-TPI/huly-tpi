package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingSubtaskResponse;
import com.huly.backend.domain.dto.pending.TogglePendingSubtaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingSubtask;
import com.huly.backend.domain.repository.pending.PendingSubtaskRepository;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TogglePendingSubtaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingSubtaskRepository pendingSubtaskRepository;
    private final PendingTaskMapper mapper;

    public PendingSubtaskResponse execute(TogglePendingSubtaskRequest request) {
        pendingTaskRepository.findByIdAndUserId(request.taskId(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.taskId()));

        pendingSubtaskRepository.findByIdAndTaskId(request.subtaskId(), request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Subtarea", "id", request.subtaskId()));

        PendingSubtask toggled = toggleSubtask(request.subtaskId());
        return mapper.toResponse(toggled);
    }

    private PendingSubtask toggleSubtask(Long subtaskId) {
        return pendingSubtaskRepository.toggle(subtaskId);
    }
}
