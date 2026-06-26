package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusResponse;
import com.huly.backend.domain.mapper.pushNotification.GetPushSubscriptionStatusMapper;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetPushSubscriptionStatusUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final GetPushSubscriptionStatusMapper mapper;

    public GetPushSubscriptionStatusResponse execute(GetPushSubscriptionStatusRequest request) {
        return mapper.toResponse(pushSubscriptionRepository.findByUserId(request.userId()).isPresent());
    }
}
