package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.repository.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CompleteTutorialUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional
    public void execute(Long userId) {
        userDetailDomainRepository.completeTutorial(userId);
    }

    @Transactional
    public void executeProfile(Long userId) {
        userDetailDomainRepository.completeProfileTutorial(userId);
    }
}
