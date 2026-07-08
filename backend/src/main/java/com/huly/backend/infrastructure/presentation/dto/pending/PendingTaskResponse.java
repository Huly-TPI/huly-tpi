package com.huly.backend.infrastructure.presentation.dto.pending;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PendingTaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        String estimatedDuration,
        String category,
        String status,
        List<PendingSubtaskResponse> subtasks,
        Double positionX,
        Double positionY,
        Double rotationDeg,
        Instant pinnedAt,
        boolean recommended,
        Instant createdAt,
        Instant completedAt
) {}
