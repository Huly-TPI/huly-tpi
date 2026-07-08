package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.user.GetUserCoinsRequest;
import com.huly.backend.domain.dto.user.GetUserCoinsResponse;
import com.huly.backend.domain.mapper.user.GetUserCoinsMapper;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserCoinsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUserCoinsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserCoinsUseCase(userRepository, new GetUserCoinsMapper());
    }

    @Test
    @DisplayName("Devuelve las monedas del usuario cuando tiene saldo")
    void executeReturnsCoinsWhenUserHasCoins() {
        givenUserCoins(10L, 500);

        GetUserCoinsResponse result = getCoins(10L);

        thenCoinsAre(result, 500);
        thenRepositoryWasQueried(10L);
    }

    @Test
    @DisplayName("Devuelve cero cuando el usuario no tiene monedas")
    void executeReturnsZeroWhenUserHasNoCoins() {
        givenUserCoins(10L, 0);

        GetUserCoinsResponse result = getCoins(10L);

        thenCoinsAre(result, 0);
    }

    @Test
    @DisplayName("Delega en el repositorio con el id de usuario recibido")
    void executeDelegatesToRepositoryWithCorrectUserId() {
        givenUserCoins(42L, 1500);

        getCoins(42L);

        thenRepositoryWasQueried(42L);
    }

    // --- arrange ---

    private void givenUserCoins(long userId, int coins) {
        when(userRepository.getCoins(userId)).thenReturn(coins);
    }

    // --- act ---

    private GetUserCoinsResponse getCoins(long userId) {
        return useCase.execute(new GetUserCoinsRequest(userId));
    }

    // --- assert ---

    private void thenCoinsAre(GetUserCoinsResponse result, int expected) {
        assertThat(result.coins()).isEqualTo(expected);
    }

    private void thenRepositoryWasQueried(long userId) {
        verify(userRepository).getCoins(userId);
    }
}
