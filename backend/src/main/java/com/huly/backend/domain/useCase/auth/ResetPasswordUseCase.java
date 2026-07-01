package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHasherPort passwordHasherPort;

    @Transactional
    public void execute(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o expirado"));

        if (Instant.now().isAfter(resetToken.getExpiresAt())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new ResourceNotFoundException("Token inválido o expirado");
        }

        String encodedPassword = passwordHasherPort.encode(newPassword);
        userRepository.updatePassword(resetToken.getUserId(), encodedPassword);
        passwordResetTokenRepository.delete(resetToken);
    }
}
