package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.CompletePendingTaskRequest;
import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

import static java.time.Instant.now;

@RequiredArgsConstructor
public class CompletePendingTaskUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingTaskMapper mapper;

    public PendingTaskResponse execute(CompletePendingTaskRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));

        PendingTask updated = completeTask(task);
        return mapper.toResponse(updated, false);
    }

    private PendingTask completeTask(PendingTask task) {
        Instant now = now();
        task.complete(now());
        return pendingTaskRepository.markCompleted(task.getId(), now);
    }
}
