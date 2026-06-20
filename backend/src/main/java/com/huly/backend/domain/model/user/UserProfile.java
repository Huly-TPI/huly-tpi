package com.huly.backend.domain.model.user;

public record UserProfile(
        AppUser user,
        boolean onBoardingCompleted,
        boolean onboardingTutorialCompleted,
        boolean profileOnboardingTutorialCompleted
) {}
