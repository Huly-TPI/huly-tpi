package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AppUser execute(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}