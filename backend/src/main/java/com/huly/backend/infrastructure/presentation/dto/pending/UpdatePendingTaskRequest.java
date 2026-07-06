package com.huly.backend.infrastructure.presentation.dto.pending;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePendingTaskRequest(
        @NotBlank(message = "El título no puede estar vacío")
        @Size(max = 120, message = "El título no puede superar los 120 caracteres")
        String title,
        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
        String description,
        LocalDate dueDate,
        String estimatedDuration,
        String category
) {}
