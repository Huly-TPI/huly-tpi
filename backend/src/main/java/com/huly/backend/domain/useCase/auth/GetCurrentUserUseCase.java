package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.UserProfile;
import com.huly.backend.domain.repository.UserDetailDomainRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.presentation.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional(readOnly = true)
    public UserProfile execute(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        boolean onBoardingCompleted = userDetailDomainRepository
                .findOnBoardingCompleted(userId).orElse(false);
        boolean onboardingTutorialCompleted = userDetailDomainRepository
                .findOnboardingTutorialCompleted(userId).orElse(false);

        return new UserProfile(user, onBoardingCompleted, onboardingTutorialCompleted);
    }
}
