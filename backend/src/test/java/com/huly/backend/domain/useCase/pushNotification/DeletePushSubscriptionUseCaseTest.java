package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionResponse;
import com.huly.backend.domain.mapper.pushNotification.DeletePushSubscriptionMapper;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeletePushSubscriptionUseCaseTest {

    @Mock
    private PushSubscriptionRepository repository;

    private DeletePushSubscriptionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePushSubscriptionUseCase(repository, new DeletePushSubscriptionMapper());
    }

    @Test
    void execute_shouldDelegateDeleteToRepository() {
        DeletePushSubscriptionResponse result = useCase.execute(
                new DeletePushSubscriptionRequest("https://fcm.example.com/abc"));
        verify(repository).deleteByEndpoint("https://fcm.example.com/abc");
        assertThat(result.deleted()).isTrue();
    }

}
