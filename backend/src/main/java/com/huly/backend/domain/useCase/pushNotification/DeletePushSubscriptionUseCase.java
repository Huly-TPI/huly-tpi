package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionResponse;
import com.huly.backend.domain.mapper.pushNotification.DeletePushSubscriptionMapper;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePushSubscriptionUseCase {

    private final PushSubscriptionRepository repository;
    private final DeletePushSubscriptionMapper mapper;

    public DeletePushSubscriptionResponse execute(DeletePushSubscriptionRequest request) {
        repository.deleteByEndpoint(request.endpoint());
        return mapper.toResponse();
    }
}
