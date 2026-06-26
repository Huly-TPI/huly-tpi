package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionResponse;
import com.huly.backend.domain.mapper.pushNotification.SavePushSubscriptionMapper;
import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SavePushSubscriptionUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final SavePushSubscriptionMapper mapper;

    public SavePushSubscriptionResponse execute(SavePushSubscriptionRequest request) {
        if (pushSubscriptionRepository.existsByEndpoint(request.endpoint())) {
            return SavePushSubscriptionResponse.notSaved();
        }

        PushSubscription saved = pushSubscriptionRepository.save(mapper.toModel(request));
        return mapper.toResponse(saved);
    }
}
