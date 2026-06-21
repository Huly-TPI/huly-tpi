package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPushSubscriptionStatusUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public boolean execute(Long userId) {
        return pushSubscriptionRepository.findByUserId(userId).isPresent();
    }
}
