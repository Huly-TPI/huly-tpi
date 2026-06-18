package com.huly.backend.infrastructure.presentation.dto.lanternRecommendation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LanternRecommendationRequest(
        @NotEmpty(message = "Debe incluir al menos un pensamiento")
        List<@NotBlank(message = "Los pensamientos no pueden estar vacíos") String> thoughts
) {}
