package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusRequest;
import com.huly.backend.domain.mapper.pushNotification.GetPushSubscriptionStatusMapper;
import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPushSubscriptionStatusUseCaseTest {

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    private GetPushSubscriptionStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPushSubscriptionStatusUseCase(pushSubscriptionRepository, new GetPushSubscriptionStatusMapper());
    }

    @Test
    void execute_shouldReturnTrue_whenUserHasSuscription() {
        when(pushSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(PushSubscription.builder().userId(1L).build()));
        assertThat(useCase.execute(new GetPushSubscriptionStatusRequest(1L)).subscribed()).isTrue();
    }

    @Test
    void execute_shouldReturnFalse_whenUserHasNoSuscription() {
        when(pushSubscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThat(useCase.execute(new GetPushSubscriptionStatusRequest(1L)).subscribed()).isFalse();
    }
}
