package com.huly.backend.domain.dto.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;

import java.time.LocalDate;
import java.util.List;

public record CreatePendingTaskRequest(
        Long userId,
        String title,
        String description,
        LocalDate dueDate,
        EstimatedDuration estimatedDuration,
        PendingCategory category,
        List<String> initialSubtaskTexts
) {}
