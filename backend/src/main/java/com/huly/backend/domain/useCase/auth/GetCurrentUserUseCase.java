package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class GetCurrentUserUseCase {

    private final UserRepository userRepository;
    private final UserDetailDomainRepository userDetailDomainRepository;

    @Transactional(readOnly = true)
    public UserProfile execute(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean onBoardingCompleted = userDetailDomainRepository
                .findOnBoardingCompleted(userId).orElse(false);
        boolean onboardingTutorialCompleted = userDetailDomainRepository
                .findOnboardingTutorialCompleted(userId).orElse(false);
        boolean profileOnboardingTutorialCompleted = userDetailDomainRepository
                .findProfileOnboardingTutorialCompleted(userId).orElse(false);

        return new UserProfile(user, onBoardingCompleted, onboardingTutorialCompleted, profileOnboardingTutorialCompleted);
    }
}
