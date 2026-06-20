package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDetailDomainRepositoryImpl implements UserDetailDomainRepository {

    private final UserDetailRepository userDetailRepository;

    @Override
    public Optional<Boolean> findOnBoardingCompleted(Long userId) {
        return userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .map(UserDetailEntity::getOnBoardingCompleted);
    }

    @Override
    public Optional<Boolean> findOnboardingTutorialCompleted(Long userId) {
        return userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .map(UserDetailEntity::getOnboardingTutorialCompleted);
    }

    @Override
    public Optional<Boolean> findProfileOnboardingTutorialCompleted(Long userId) {
        return userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .map(UserDetailEntity::getProfileOnboardingTutorialCompleted);
    }

    @Override
    public ThemePreference findThemePreference(Long userId) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        return userDetail.getThemePreference();
    }

    @Override
    @Transactional
    public void completeOnboarding(Long userId, String answer1, String answer2, String answer3) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        userDetail.setOnboardingAnswer1(answer1);
        userDetail.setOnboardingAnswer2(answer2);
        userDetail.setOnboardingAnswer3(answer3);
        userDetail.setOnBoardingCompleted(true);
        userDetailRepository.save(userDetail);
    }

    @Override
    @Transactional
    public void completeTutorial(Long userId) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        userDetail.setOnboardingTutorialCompleted(true);
        userDetailRepository.save(userDetail);
    }

    @Override
    @Transactional
    public void completeProfileTutorial(Long userId) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        userDetail.setProfileOnboardingTutorialCompleted(true);
        userDetailRepository.save(userDetail);
    }

    @Override
    @Transactional
    public void updateThemePreference(Long userId, ThemePreference themePreference) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        userDetail.setThemePreference(themePreference);
        userDetailRepository.save(userDetail);
    }

    @Override
    public DailyClaimState findDailyClaimState(Long userId) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        int streak = userDetail.getDailyRewardStreak() != null ? userDetail.getDailyRewardStreak() : 0;
        return new DailyClaimState(streak, userDetail.getLastDailyClaimDate());
    }

    @Override
    @Transactional
    public void updateDailyClaim(Long userId, int streak, LocalDate claimDate) {
        UserDetailEntity userDetail = userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("No se encontraron datos del usuario: " + userId));
        userDetail.setDailyRewardStreak(streak);
        userDetail.setLastDailyClaimDate(claimDate);
        userDetailRepository.save(userDetail);
    }

}
