package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.exception.InsufficientCoinsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoinServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CoinService coinService;

    @Test
    void credit_shouldCallAddCoins_withCorrectUserIdAndAmount() {
        coinService.credit(42L, 500);

        verify(userRepository).addCoins(42L, 500);
    }

    @Test
    void debit_shouldCallDebitCoins_whenBalanceIsSufficiente() {
        when(userRepository.debitCoins(42L, 500)).thenReturn(1);
        coinService.debit(42L, 500);
        verify(userRepository).debitCoins(42L, 500);
    }

    @Test
    void debit_shouldThrowException_whenBalanceIsInsufficient() {
        when(userRepository.debitCoins(42L, 500)).thenReturn(0);
        assertThatThrownBy(() -> coinService.debit(42L, 500)).isInstanceOf(InsufficientCoinsException.class);
    }
}
