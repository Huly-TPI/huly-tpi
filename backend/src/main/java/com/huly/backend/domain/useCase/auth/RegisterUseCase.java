package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.auth.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasherPort;
    private final LoginUseCase loginUseCase;

    @Transactional
    public AuthTokens execute(String email, String rawPassword, String name, LocalDate birthDate) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("el email esta en uso");
        }

        userRepository.save(AppUser.builder()
                .name(name)
                .email(email)
                .password(passwordHasherPort.encode(rawPassword))
                .birthDate(birthDate)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

            return loginUseCase.execute(email, rawPassword);
    }
}
