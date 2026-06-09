package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserCoinsUseCaseTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private GetUserCoinsUseCase useCase;

    @Test
    void execute_shouldReturnCoins_whenUserHasCoins() {
        when(userRepository.getCoins(10L)).thenReturn(500);

        int result = useCase.execute(10L);

        assertThat(result).isEqualTo(500);
        verify(userRepository).getCoins(10L);
    }

    @Test
    void execute_shouldReturnZero_whenUserHasNoCoins() {
        when(userRepository.getCoins(10L)).thenReturn(0);

        int result = useCase.execute(10L);

        assertThat(result).isZero();
    }

    @Test
    void execute_shouldDelegateToRepository_withCorrectUserId() {
        when(userRepository.getCoins(42L)).thenReturn(1500);

        useCase.execute(42L);

        verify(userRepository).getCoins(42L);
    }
}
