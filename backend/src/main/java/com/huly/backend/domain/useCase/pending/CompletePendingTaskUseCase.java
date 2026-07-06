package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.CompletePendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
public class CompletePendingTaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingTaskMapper mapper;

    public PendingTaskResponse execute(CompletePendingTaskRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));

        Instant now = Instant.now();
        task.complete(now);

        PendingTask updated = pendingTaskRepository.markCompleted(request.id(), now);
        return mapper.toResponse(updated, false);
    }
}
