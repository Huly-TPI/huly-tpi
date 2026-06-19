package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.enums.ThemePreference;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDetailDomainRepository {
    Optional<Boolean> findOnBoardingCompleted(Long userId);
    Optional<Boolean> findOnboardingTutorialCompleted(Long userId);
    Optional<Boolean> findProfileOnboardingTutorialCompleted(Long userId);
    ThemePreference findThemePreference(Long userId);
    void completeOnboarding(Long userId, String answer1, String answer2, String answer3);
    void completeTutorial(Long userId);
    void completeProfileTutorial(Long userId);
    void updateThemePreference(Long userId, ThemePreference themePreference);

    /** Estado actual de la racha de recompensas diarias del usuario. */
    DailyClaimState findDailyClaimState(Long userId);

    /** Persiste el reclamo diario: nueva racha y fecha del reclamo. */
    void updateDailyClaim(Long userId, int streak, LocalDate claimDate);
}
