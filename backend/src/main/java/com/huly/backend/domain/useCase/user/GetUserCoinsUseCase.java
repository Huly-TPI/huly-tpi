package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetUserCoinsUseCase {

    private final UserRepository userRepository;

    public int execute(Long userId) {
        return userRepository.getCoins(userId);
    }
}
