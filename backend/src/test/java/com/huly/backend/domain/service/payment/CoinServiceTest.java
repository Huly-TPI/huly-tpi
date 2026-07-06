package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.exception.InsufficientCoinsException;
import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoinServiceTest {

    private static final Long USER_ID = 42L;
    private static final int AMOUNT = 500;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CoinService coinService;

    @Test
    @DisplayName("Acredita monedas delegando en el repositorio")
    void creditShouldCallAddCoinsWithCorrectUserIdAndAmount() {
        credit();

        thenAddCoinsCalled();
    }

    @Test
    @DisplayName("Debita monedas cuando el saldo es suficiente")
    void debitShouldCallDebitCoinsWhenBalanceIsSufficient() {
        givenDebitSucceeds();

        debit();

        thenDebitCoinsCalled();
    }

    @Test
    @DisplayName("Lanza excepción cuando el saldo es insuficiente")
    void debitShouldThrowExceptionWhenBalanceIsInsufficient() {
        givenDebitFails();

        thenDebitThrowsInsufficientCoins();
    }

    // --- arrange ---
    private void givenDebitSucceeds() {
        when(userRepository.debitCoins(USER_ID, AMOUNT)).thenReturn(1);
    }

    private void givenDebitFails() {
        when(userRepository.debitCoins(USER_ID, AMOUNT)).thenReturn(0);
    }

    // --- act ---
    private void credit() {
        coinService.credit(USER_ID, AMOUNT);
    }

    private void debit() {
        coinService.debit(USER_ID, AMOUNT);
    }

    // --- assert ---
    private void thenAddCoinsCalled() {
        verify(userRepository).addCoins(USER_ID, AMOUNT);
    }

    private void thenDebitCoinsCalled() {
        verify(userRepository).debitCoins(USER_ID, AMOUNT);
    }

    private void thenDebitThrowsInsufficientCoins() {
        assertThatThrownBy(this::debit).isInstanceOf(InsufficientCoinsException.class);
    }
}
