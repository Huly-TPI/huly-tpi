package com.huly.backend.infrastructure.presentation.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WebhookNotificationDto(
        @JsonProperty("id") Long id,
        @JsonProperty("live_mode") Boolean liveMode,
        @JsonProperty("type") String type,
        @JsonProperty("date_created") String dateCreated,
        @JsonProperty("user_id") String userId,
        @JsonProperty("api_version") String apiVersion,
        @JsonProperty("action") String action,
        @JsonProperty("data") WebhookDataDto data
) {
    public record WebhookDataDto(
            @JsonProperty("id") String id
    ) {}
}
