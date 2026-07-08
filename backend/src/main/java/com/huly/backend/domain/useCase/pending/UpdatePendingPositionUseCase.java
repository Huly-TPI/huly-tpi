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

import static java.time.Instant.now;

@RequiredArgsConstructor
public class UpdatePendingPositionUseCase {

    private final PendingTaskRepository pendingTaskRepository;
    private final PendingTaskMapper mapper;
    private final DoubleSupplier randomRotationSupplier;

    public PendingTaskResponse execute(UpdatePendingPositionRequest request) {
        PendingTask task = pendingTaskRepository.findByIdAndUserId(request.id(), request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Pending", "id", request.id()));

        boolean firstPin = !task.isPlaced();
        Double assignedRotation = resolveRotation(task, firstPin);

        PendingTask updated = updatePosition(task, request, assignedRotation, firstPin);
        return mapper.toResponse(updated, false);
    }

    private Double resolveRotation(PendingTask task, boolean firstPin) {
        return firstPin ? randomRotationSupplier.getAsDouble() : task.getRotationDeg();
    }

    private PendingTask updatePosition(PendingTask task, UpdatePendingPositionRequest request, Double rotation, boolean firstPin) {
        Instant now = now();
        task.applyPosition(request.positionX(), request.positionY(), rotation, now);
        return pendingTaskRepository.updatePosition(
                request.id(), request.positionX(), request.positionY(), firstPin ? rotation : null, now);
    }
}
