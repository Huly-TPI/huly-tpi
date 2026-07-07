package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionResponse;
import com.huly.backend.domain.mapper.pushNotification.DeletePushSubscriptionMapper;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeletePushSubscriptionUseCaseTest {

    private static final String ENDPOINT = "https://fcm.example.com/abc";

    @Mock
    private PushSubscriptionRepository repository;

    private DeletePushSubscriptionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeletePushSubscriptionUseCase(repository, new DeletePushSubscriptionMapper());
    }

    @Test
    @DisplayName("Delega la eliminación en el repositorio y devuelve eliminado")
    void executeShouldDelegateDeleteToRepository() {
        // --- act ---
        DeletePushSubscriptionResponse result = delete();
        // --- assert ---
        thenDeletedFromRepository(result);
    }

    // --- act ---

    private DeletePushSubscriptionResponse delete() {
        return useCase.execute(new DeletePushSubscriptionRequest(ENDPOINT));
    }

    // --- assert ---

    private void thenDeletedFromRepository(DeletePushSubscriptionResponse result) {
        verify(repository).deleteByEndpoint(ENDPOINT);
        assertThat(result.deleted()).isTrue();
    }
}
