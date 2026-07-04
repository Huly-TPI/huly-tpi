package com.huly.backend.domain.dto.pushNotification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateNotificationHourRequest(
        @Min(0) @Max(23) int hour
) {
}