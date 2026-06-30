package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class RequestPasswordResetUseCase {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailPort emailPort;

    private String generateToken() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void execute(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cuenta con ese email"));

        passwordResetTokenRepository.deleteAllByUserId(user.getId());

        String token = generateToken();
        Instant now = Instant.now();

        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .userId(user.getId())
                .token(token)
                .createdAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .build());

        emailPort.sendPasswordReset(user.getEmail(), token);
    }
}
