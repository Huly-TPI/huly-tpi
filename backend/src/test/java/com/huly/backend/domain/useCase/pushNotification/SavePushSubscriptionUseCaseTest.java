package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionResponse;
import com.huly.backend.domain.mapper.pushNotification.SavePushSubscriptionMapper;
import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavePushSubscriptionUseCaseTest {

    private static final Long USER_ID = 10L;
    private static final Long SAVED_ID = 1L;
    private static final String ENDPOINT = "https://fcm.example.com/abc";
    private static final String P256DH = "key123";
    private static final String AUTH = "auth123";

    @Mock
    private PushSubscriptionRepository repository;

    private SavePushSubscriptionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SavePushSubscriptionUseCase(repository, new SavePushSubscriptionMapper());
    }

    @Test
    @DisplayName("Guarda la suscripción cuando el endpoint no existe")
    void executeShouldSaveSubscriptionWhenEndpointDoesNotExist() {
        // --- arrange ---
        givenEndpointDoesNotExist();
        givenSaveReturnsSubscription();
        // --- act ---
        SavePushSubscriptionResponse result = save();
        // --- assert ---
        thenSubscriptionSaved(result);
    }

    @Test
    @DisplayName("No guarda la suscripción cuando el endpoint ya existe")
    void executeShouldNotSaveWhenEndpointAlreadyExists() {
        // --- arrange ---
        givenEndpointAlreadyExists();
        // --- act ---
        SavePushSubscriptionResponse result = save();
        // --- assert ---
        thenSubscriptionNotSaved(result);
    }

    // --- arrange ---

    private void givenEndpointDoesNotExist() {
        when(repository.existsByEndpoint(ENDPOINT)).thenReturn(false);
    }

    private void givenEndpointAlreadyExists() {
        when(repository.existsByEndpoint(ENDPOINT)).thenReturn(true);
    }

    private void givenSaveReturnsSubscription() {
        when(repository.save(any())).thenReturn(PushSubscription.builder()
                .id(SAVED_ID).userId(USER_ID).endpoint(ENDPOINT).p256dh(P256DH).auth(AUTH).build());
    }

    // --- act ---

    private SavePushSubscriptionResponse save() {
        return useCase.execute(new SavePushSubscriptionRequest(USER_ID, ENDPOINT, P256DH, AUTH));
    }

    // --- assert ---

    private void thenSubscriptionSaved(SavePushSubscriptionResponse result) {
        ArgumentCaptor<PushSubscription> captor = ArgumentCaptor.forClass(PushSubscription.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getEndpoint()).isEqualTo(ENDPOINT);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(result.saved()).isTrue();
        assertThat(result.id()).isEqualTo(SAVED_ID);
    }

    private void thenSubscriptionNotSaved(SavePushSubscriptionResponse result) {
        assertThat(result.saved()).isFalse();
        verify(repository, never()).save(any());
    }
}
