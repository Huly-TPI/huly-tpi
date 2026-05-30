package com.huly.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CloudRecommendationRequest(
        @NotEmpty(message = "Debe incluir al menos un pensamiento")
        List<@NotBlank(message = "Los pensamientos no pueden estar vacíos") String> thoughts
) {}
