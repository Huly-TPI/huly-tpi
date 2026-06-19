package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CoinServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CoinService coinService;

    @Test
    void credit_shouldCallAddCoins_withCorrectUserIdAndAmount() {
        coinService.credit(42L, 500);

        verify(userRepository).addCoins(42L, 500);
    }
}
