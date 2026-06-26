package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.dto.onboarding.CompleteTutorialRequest;
import com.huly.backend.domain.dto.onboarding.CompleteTutorialResponse;
import com.huly.backend.domain.mapper.onboarding.CompleteTutorialMapper;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CompleteTutorialUseCase {

    private final UserDetailDomainRepository userDetailDomainRepository;
    private final CompleteTutorialMapper mapper;

    @Transactional
    public CompleteTutorialResponse execute(CompleteTutorialRequest request) {
        userDetailDomainRepository.completeTutorial(request.userId());
        return mapper.toResponse();
    }

    @Transactional
    public CompleteTutorialResponse executeProfile(CompleteTutorialRequest request) {
        userDetailDomainRepository.completeProfileTutorial(request.userId());
        return mapper.toResponse();
    }
}
