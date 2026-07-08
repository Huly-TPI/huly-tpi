package com.huly.backend.domain.dto.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;
import com.huly.backend.domain.model.enums.PendingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record PendingTaskResponse(
        Long id,
        String title,
        String description,
        LocalDate dueDate,
        EstimatedDuration estimatedDuration,
        PendingCategory category,
        PendingStatus status,
        List<PendingSubtaskResponse> subtasks,
        Double positionX,
        Double positionY,
        Double rotationDeg,
        Instant pinnedAt,
        boolean recommended,
        Instant createdAt,
        Instant completedAt
) {}
