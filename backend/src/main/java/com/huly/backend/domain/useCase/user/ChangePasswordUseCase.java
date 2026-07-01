package com.huly.backend.domain.useCase.user;

import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.port.PasswordHasherPort;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordHasherPort passwordHasherPort;

    @Transactional
    public void execute(Long userId, String currentPassword, String newPassword) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordHasherPort.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        String encodedNewPassword = passwordHasherPort.encode(newPassword);
        userRepository.updatePassword(userId, encodedNewPassword);
    }
}
