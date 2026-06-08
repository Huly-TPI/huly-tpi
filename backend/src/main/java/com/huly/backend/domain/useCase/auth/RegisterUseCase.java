package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final LoginUseCase loginUseCase;

    @Transactional
    public AuthTokens execute(String email, String rawPassword, String name, LocalDate birthDate) {

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use");
        }

        userRepository.save(AppUser.builder()
                .name(name)
                .email(email)
                .password(passwordHasher.encode(rawPassword))
                .birthDate(birthDate)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

            return loginUseCase.execute(email, rawPassword);
    }
}
