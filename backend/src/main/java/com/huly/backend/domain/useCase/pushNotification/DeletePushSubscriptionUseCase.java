package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletePushSubscriptionUseCase {

    private final PushSubscriptionRepository repository;

    public void execute(String endpoint) {
        repository.deleteByEndpoint(endpoint);
    }
    
}
