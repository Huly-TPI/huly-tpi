package com.huly.backend.infrastructure.presentation.dto.pushNotification;

public record PushSubscriptionStatusResponse(boolean subscribed, int notificationHour) {
}