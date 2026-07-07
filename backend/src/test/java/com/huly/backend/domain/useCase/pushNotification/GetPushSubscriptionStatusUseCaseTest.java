package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusResponse;
import com.huly.backend.domain.mapper.pushNotification.GetPushSubscriptionStatusMapper;
import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPushSubscriptionStatusUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final int CUSTOM_HOUR = 20;
    private static final int DEFAULT_HOUR = 9;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    private GetPushSubscriptionStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPushSubscriptionStatusUseCase(pushSubscriptionRepository, new GetPushSubscriptionStatusMapper());
    }

    @Test
    @DisplayName("Devuelve suscrito y la hora configurada cuando el usuario tiene suscripción")
    void executeShouldReturnTrueWhenUserHasSubscription() {
        // --- arrange ---
        givenUserHasSubscription();
        // --- act ---
        GetPushSubscriptionStatusResponse result = status();
        // --- assert ---
        thenSubscribed(result);
    }

    @Test
    @DisplayName("Devuelve no suscrito y la hora por defecto cuando el usuario no tiene suscripción")
    void executeShouldReturnFalseWhenUserHasNoSubscription() {
        // --- arrange ---
        givenUserHasNoSubscription();
        // --- act ---
        GetPushSubscriptionStatusResponse result = status();
        // --- assert ---
        thenNotSubscribed(result);
    }

    // --- arrange ---

    private void givenUserHasSubscription() {
        when(pushSubscriptionRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(PushSubscription.builder().userId(USER_ID).notificationHour(CUSTOM_HOUR).build()));
    }

    private void givenUserHasNoSubscription() {
        when(pushSubscriptionRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    // --- act ---

    private GetPushSubscriptionStatusResponse status() {
        return useCase.execute(new GetPushSubscriptionStatusRequest(USER_ID));
    }

    // --- assert ---

    private void thenSubscribed(GetPushSubscriptionStatusResponse result) {
        assertThat(result.subscribed()).isTrue();
        assertThat(result.notificationHour()).isEqualTo(CUSTOM_HOUR);
    }

    private void thenNotSubscribed(GetPushSubscriptionStatusResponse result) {
        assertThat(result.subscribed()).isFalse();
        assertThat(result.notificationHour()).isEqualTo(DEFAULT_HOUR);
    }
}
