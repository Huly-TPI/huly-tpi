package com.huly.backend.presentation.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CloudRecommendationRequest(
        @NotEmpty
        List<String> thoughts
) {}
