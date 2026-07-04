package com.huly.backend.domain.dto.pushNotification;
public record GetPushSubscriptionStatusResponse(boolean subscribed, int notificationHour) {
}
