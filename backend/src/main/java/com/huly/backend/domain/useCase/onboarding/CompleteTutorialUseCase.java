package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteTutorialUseCase {

    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional
    public void execute(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + email));

        userDetailDomainRepository.completeTutorial(user.getId());
    }
}
