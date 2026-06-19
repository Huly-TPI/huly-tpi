package com.huly.backend.domain.service.payment;

import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoinService {

    private final UserRepository userRepository;

    public void credit(Long userId, int amount) {
        userRepository.addCoins(userId, amount);
    }
}
