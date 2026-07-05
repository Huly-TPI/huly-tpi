package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.user.AudioSettings;
import com.huly.backend.domain.model.user.UserAccountSettings;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDetailDomainRepository {
    Optional<Boolean> findOnBoardingCompleted(Long userId);
    Optional<Boolean> findOnboardingTutorialCompleted(Long userId);
    Optional<Boolean> findProfileOnboardingTutorialCompleted(Long userId);
    ThemePreference findThemePreference(Long userId);
    AudioSettings findAudioSettings(Long userId);
    UserAccountSettings findAccountSettings(Long userId);
    void completeOnboarding(Long userId, String answer1, String answer2, String answer3);
    void completeTutorial(Long userId);
    void completeProfileTutorial(Long userId);
    void updateThemePreference(Long userId, ThemePreference themePreference);
    AudioSettings updateAudioSettings(Long userId, AudioSettings audioSettings);
    UserAccountSettings updateAccountSettings(Long userId, UserAccountSettings accountSettings);

    /** Estado actual de la racha de recompensas diarias del usuario. */
    DailyClaimState findDailyClaimState(Long userId);

    /** Persiste el reclamo diario: nueva racha y fecha del reclamo. */
    void updateDailyClaim(Long userId, int streak, LocalDate claimDate);

    /** Fecha de la última actividad registrada (base de la recompensa de regreso). */
    Optional<LocalDate> findLastLoginDate(Long userId);

    /** Registra la actividad de hoy (resetea la brecha de inactividad). */
    void updateLastLoginDate(Long userId, LocalDate date);
}
