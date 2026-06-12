package com.huly.backend.domain.useCase.lead;

import com.huly.backend.domain.exception.DuplicateResourceException;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.port.EmailPort;
import com.huly.backend.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RegisterLeadUseCase {

    private final UserRepository userRepository;
    private final EmailPort emailPort;

    @Transactional
    public void execute(String email, String nickname, SourceAction sourceAction) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("lead", "email", email);
        }
        AppUser saved = userRepository.save(AppUser.builder()
                .email(email)
                .role(UserRole.LEAD)
                .status(UserStatus.ACTIVE)
                .build());
        userRepository.saveLeadDetail(saved.getId(), nickname, sourceAction);
        emailPort.sendWelcomeLead(email, nickname);
    }
}
