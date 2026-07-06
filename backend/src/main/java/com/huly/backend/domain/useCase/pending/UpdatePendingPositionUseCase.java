package com.huly.backend.domain.useCase.pending;

import com.huly.backend.domain.dto.pending.PendingTaskResponse;
import com.huly.backend.domain.dto.pending.UpdatePendingPositionRequest;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.mapper.pending.PendingTaskMapper;
import com.huly.backend.domain.model.pending.PendingTask;
import com.huly.backend.domain.repository.pending.PendingTaskRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.DoubleSupplier;

@RequiredArgsConstructor
public class UpdatePendingPositionUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingTaskMapper mapper;
    private final DoubleSupplier randomRotationSupplier;

    public PendingTaskResponse execute(UpdatePendingPositionRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));

        boolean firstPin = !task.isPlaced();
        Double assignedRotation = firstPin ? randomRotationSupplier.getAsDouble() : task.getRotationDeg();
        Instant now = Instant.now();

        task.applyPosition(request.positionX(), request.positionY(), assignedRotation, now);

        PendingTask updated = pendingTaskRepository.updatePosition(
                request.id(), request.positionX(), request.positionY(), firstPin ? assignedRotation : null, now);
        return mapper.toResponse(updated, false);
    }
}
