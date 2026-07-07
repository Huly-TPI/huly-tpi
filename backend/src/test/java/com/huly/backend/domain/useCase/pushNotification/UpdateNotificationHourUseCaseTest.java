package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateNotificationHourUseCaseTest {

    private static final Long USER_ID = 7L;
    private static final int HOUR = 20;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    private UpdateNotificationHourUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateNotificationHourUseCase(pushSubscriptionRepository);
    }

    @Test
    @DisplayName("Delega la actualización de la hora de notificación en el repositorio")
    void executeShouldDelegateToRepository() {
        // --- act ---
        updateHour();
        // --- assert ---
        thenDelegatedToRepository();
    }

    // --- act ---

    private void updateHour() {
        useCase.execute(USER_ID, HOUR);
    }

    // --- assert ---

    private void thenDelegatedToRepository() {
        verify(pushSubscriptionRepository).updateNotificationHourByUserId(USER_ID, HOUR);
    }
}
