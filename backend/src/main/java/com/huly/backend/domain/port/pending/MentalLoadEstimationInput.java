package com.huly.backend.domain.port.pending;

import com.huly.backend.domain.model.enums.EstimatedDuration;
import com.huly.backend.domain.model.enums.PendingCategory;

import java.time.LocalDate;

public record MentalLoadEstimationInput(
        String title,
        String description,
        LocalDate dueDate,
        Integer daysUntilDue,
        EstimatedDuration estimatedDuration,
        PendingCategory category,
        int subtaskCount
) {}
