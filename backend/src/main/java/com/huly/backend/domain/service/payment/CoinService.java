package com.huly.backend.domain.service.payment;
import com.huly.backend.domain.exception.InsufficientCoinsException;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final UserRepository userRepository;

    public void credit(Long userId, int amount) {
        userRepository.addCoins(userId, amount);
    }

    public void debit(Long userId, int amount) {
        int rowsAffected = userRepository.debitCoins(userId, amount);
        if (rowsAffected == 0) {
            throw new InsufficientCoinsException("Saldo de monedas insuficiente");
        }
    }
    
}
