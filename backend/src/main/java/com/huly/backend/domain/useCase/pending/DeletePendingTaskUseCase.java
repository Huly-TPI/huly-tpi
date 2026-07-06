package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.DeletePendingTaskRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePendingTaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;

    public void execute(DeletePendingTaskRequest request) {
        pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));
        pendingTaskRepository.delete(request.id(), request.userId());
    }
}
