package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateNotificationHourUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public void execute(Long userId, int hour) {
        pushSubscriptionRepository.updateNotificationHourByUserId(userId, hour);
    }
}