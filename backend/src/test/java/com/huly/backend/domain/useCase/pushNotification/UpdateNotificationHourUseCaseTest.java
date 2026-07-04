package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationHourUseCaseTest {

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    private UpdateNotificationHourUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateNotificationHourUseCase(pushSubscriptionRepository);
    }

    @Test
    void execute_shouldDelegateToRepository() {
        useCase.execute(7L, 20);
        verify(pushSubscriptionRepository).updateNotificationHourByUserId(7L, 20);
    }
}