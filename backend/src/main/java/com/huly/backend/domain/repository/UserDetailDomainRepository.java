package com.huly.backend.domain.repository;

import java.util.Optional;

public interface UserDetailDomainRepository {
    Optional<Boolean> findOnBoardingCompleted(Long userId);
    Optional<Boolean> findOnboardingTutorialCompleted(Long userId);
    void completeOnboarding(Long userId, String answer1, String answer2, String answer3);
    void completeTutorial(Long userId);
}
