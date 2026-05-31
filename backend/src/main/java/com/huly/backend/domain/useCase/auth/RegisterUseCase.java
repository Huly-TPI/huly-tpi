package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public void execute(String email, String rawPassword, String name) {

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use");
        }

        userRepository.save(AppUser.builder()
                .name(name)
                .email(email)
                .password(passwordHasher.encode(rawPassword))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());
    }
}
