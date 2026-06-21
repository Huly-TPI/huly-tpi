package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UnsubscribeFromEmailsUseCase {

    private final UserRepository userRepository;

    public boolean execute(String token) {
        Optional<AppUser> user = userRepository.findByUnsubscribeToken(token);
        if (user.isEmpty()) {
            return false;
        }
        userRepository.disableReengagementEmails(user.get().getId());
        return true;
    }
}
