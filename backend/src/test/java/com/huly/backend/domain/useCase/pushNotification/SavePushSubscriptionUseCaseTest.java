package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavePushSubscriptionUseCaseTest {
    
    @Mock 
    private PushSubscriptionRepository repository;

    @InjectMocks
    private SavePushSubscriptionUseCase useCase;

    @Test
    void execute_shouldSaveSubscription_whenEndpointDoesNotExist() {
        when(repository.existsByEndpoint("https://fcm.example.com/abc")).thenReturn(false);
        PushSubscription saved = PushSubscription.builder().id(1L).userId(10L).endpoint("https://fcm.example.com/abc")
        .p256dh("key123").auth("auth123").build();
        when(repository.save(any())).thenReturn(saved);

        ArgumentCaptor<PushSubscription> captor = ArgumentCaptor.forClass(PushSubscription.class);
        PushSubscription result = useCase.execute(10L, "https://fcm.example.com/abc", "key123", "auth123");

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(10L);
        assertThat(captor.getValue().getEndpoint()).isEqualTo("https://fcm.example.com/abc");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test 
    void execute_shouldNotSave_whenEndpointAlreadyExists() {
        when(repository.existsByEndpoint("https://fcm.example.com/abc")).thenReturn(true);
        useCase.execute(10L, "https://fcm.example.com/abc", "key123", "auth123");
        verify(repository, never()).save(any());
    }

}
