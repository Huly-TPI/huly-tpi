package com.huly.backend.domain.model;

public record UserProfile(
        AppUser user,
        boolean onBoardingCompleted,
        boolean onboardingTutorialCompleted,
        boolean profileOnboardingTutorialCompleted
) {}
