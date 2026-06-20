package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.user.InactiveUserToRemind;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

    /** Último login registrado (vacío si no hay detail o nunca se logueó). */
    Optional<Instant> findLastLoginDate(Long userId);

    /** Marca la fecha/hora del último login. No-op si el usuario aún no tiene detail. */
    void updateLastLoginDate(Long userId, Instant lastLogin);

    /** Usuarios inactivos (más allá del umbral) que necesitan el email recordatorio. */
    List<InactiveUserToRemind> findUsersNeedingInactivityReminder(Instant threshold);

    /** Marca que ya se envió el recordatorio de inactividad. */
    void markInactivityReminderSent(Long userId, Instant sentAt);
}
