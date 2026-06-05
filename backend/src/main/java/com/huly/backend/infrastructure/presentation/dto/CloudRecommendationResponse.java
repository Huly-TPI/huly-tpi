package com.huly.backend.infrastructure.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CloudRecommendationResponse(
        @JsonProperty("activity_type")
        String activityType,

        @JsonProperty("action_id")
        String actionId,

        String title,

        String description,

        @JsonProperty("redirect_url")
        String redirectUrl
) {}
