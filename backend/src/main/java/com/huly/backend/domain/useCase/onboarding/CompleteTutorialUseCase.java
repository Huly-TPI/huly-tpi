package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.repository.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteTutorialUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional
    public void execute(Long userId) {
        userDetailDomainRepository.completeTutorial(userId);
    }
}
