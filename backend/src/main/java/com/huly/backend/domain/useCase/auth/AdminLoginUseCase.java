package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLoginUseCase {

    private final UserRepository userRepository;
    private final LoginUseCase loginUseCase;

    public AuthTokens execute(String email, String rawPassword) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return loginUseCase.execute(email, rawPassword);
    }
}
