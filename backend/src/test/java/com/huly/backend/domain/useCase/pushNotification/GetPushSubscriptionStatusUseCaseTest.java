package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPushSubscriptionStatusUseCaseTest {

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    @InjectMocks
    private GetPushSubscriptionStatusUseCase useCase;

    @Test
    void execute_shouldReturnTrue_whenUserHasSuscription() {
        when(pushSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(PushSubscription.builder().userId(1L).build()));
        assertThat(useCase.execute(1L)).isTrue();
    }

    @Test
    void execute_shouldReturnFalse_whenUserHasNoSuscription() {
        when(pushSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThat(useCase.execute(1L)).isFalse();
    }
}
