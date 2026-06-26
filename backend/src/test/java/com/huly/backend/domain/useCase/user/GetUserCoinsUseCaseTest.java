package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.dto.user.GetUserCoinsRequest;
import com.huly.backend.domain.dto.user.GetUserCoinsResponse;
import com.huly.backend.domain.mapper.user.GetUserCoinsMapper;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserCoinsUseCaseTest {

    @Mock private UserRepository userRepository;
    private GetUserCoinsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserCoinsUseCase(userRepository, new GetUserCoinsMapper());
    }

    @Test
    void execute_shouldReturnCoins_whenUserHasCoins() {
        when(userRepository.getCoins(10L)).thenReturn(500);

        GetUserCoinsResponse result = useCase.execute(new GetUserCoinsRequest(10L));

        assertThat(result.coins()).isEqualTo(500);
        verify(userRepository).getCoins(10L);
    }

    @Test
    void execute_shouldReturnZero_whenUserHasNoCoins() {
        when(userRepository.getCoins(10L)).thenReturn(0);

        GetUserCoinsResponse result = useCase.execute(new GetUserCoinsRequest(10L));

        assertThat(result.coins()).isZero();
    }

    @Test
    void execute_shouldDelegateToRepository_withCorrectUserId() {
        when(userRepository.getCoins(42L)).thenReturn(1500);

        useCase.execute(new GetUserCoinsRequest(42L));

        verify(userRepository).getCoins(42L);
    }
}
