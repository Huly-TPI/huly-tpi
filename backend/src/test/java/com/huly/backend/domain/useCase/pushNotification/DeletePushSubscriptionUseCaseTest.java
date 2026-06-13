package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeletePushSubscriptionUseCaseTest {

    @Mock 
    private PushSubscriptionRepository repository;

    @InjectMocks
    private DeletePushSubscriptionUseCase useCase;

    @Test 
    void execute_shouldDelegateDeleteToRepository() {
        useCase.execute("https://fcm.example.com/abc");
        verify(repository).deleteByEndpoint("https://fcm.example.com/abc");
    }

}
