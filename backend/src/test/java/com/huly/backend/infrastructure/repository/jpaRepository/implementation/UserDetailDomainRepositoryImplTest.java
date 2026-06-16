package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailDomainRepositoryImplTest {

    @Mock
    private UserDetailRepository userDetailRepository;

    @InjectMocks
    private UserDetailDomainRepositoryImpl userDetailDomainRepository;

    @Test
    void findOnBoardingCompleted_shouldReturnValue_whenUserDetailExists() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(1L).onBoardingCompleted(true).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        Optional<Boolean> result = userDetailDomainRepository.findOnBoardingCompleted(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }

    @Test
    void findOnBoardingCompleted_shouldReturnEmpty_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(99L))
                .thenReturn(Optional.empty());

        Optional<Boolean> result = userDetailDomainRepository.findOnBoardingCompleted(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findOnboardingTutorialCompleted_shouldReturnValue_whenUserDetailExists() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(2L).onboardingTutorialCompleted(true).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(2L))
                .thenReturn(Optional.of(entity));

        Optional<Boolean> result = userDetailDomainRepository.findOnboardingTutorialCompleted(2L);

        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }

    @Test
    void findOnboardingTutorialCompleted_shouldReturnEmpty_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(100L))
                .thenReturn(Optional.empty());

        Optional<Boolean> result = userDetailDomainRepository.findOnboardingTutorialCompleted(100L);

        assertThat(result).isEmpty();
    }

    @Test
    void findProfileOnboardingTutorialCompleted_shouldReturnValue_whenUserDetailExists() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(2L).profileOnboardingTutorialCompleted(true).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(2L))
                .thenReturn(Optional.of(entity));

        Optional<Boolean> result = userDetailDomainRepository.findProfileOnboardingTutorialCompleted(2L);

        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }

    @Test
    void findThemePreference_shouldReturnValue_whenUserDetailExists() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(4L).themePreference(ThemePreference.DARK).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(4L))
                .thenReturn(Optional.of(entity));

        ThemePreference result = userDetailDomainRepository.findThemePreference(4L);

        assertThat(result).isEqualTo(ThemePreference.DARK);
    }

    @Test
    void findThemePreference_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(104L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.findThemePreference(104L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void completeOnboarding_shouldSetAnswersAndMarkCompleted() {
        UserDetailEntity entity = UserDetailEntity.builder().id(1L).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        userDetailDomainRepository.completeOnboarding(1L, "Calmar mi mente", "Soltar el control",
                "Respirar antes de reaccionar");

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        UserDetailEntity saved = captor.getValue();
        assertThat(saved.getOnboardingAnswer1()).isEqualTo("Calmar mi mente");
        assertThat(saved.getOnboardingAnswer2()).isEqualTo("Soltar el control");
        assertThat(saved.getOnboardingAnswer3()).isEqualTo("Respirar antes de reaccionar");
        assertThat(saved.getOnBoardingCompleted()).isTrue();
        assertThat(saved.getProfileOnBoardingCompleted()).isNull();
    }

    @Test
    void completeOnboarding_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.completeOnboarding(99L, "A", "B", "C"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void completeTutorial_shouldMarkTutorialCompleted() {
        UserDetailEntity entity = UserDetailEntity.builder().id(3L).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(3L))
                .thenReturn(Optional.of(entity));

        userDetailDomainRepository.completeTutorial(3L);

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getOnboardingTutorialCompleted()).isTrue();
    }

    @Test
    void completeTutorial_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(101L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.completeTutorial(101L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void completeProfileTutorial_shouldMarkProfileTutorialCompleted() {
        UserDetailEntity entity = UserDetailEntity.builder().id(3L).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(3L))
                .thenReturn(Optional.of(entity));

        userDetailDomainRepository.completeProfileTutorial(3L);

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getProfileOnboardingTutorialCompleted()).isTrue();
    }

    @Test
    void updateThemePreference_shouldPersistThemePreference() {
        UserDetailEntity entity = UserDetailEntity.builder().id(5L).themePreference(ThemePreference.LIGHT).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(5L))
                .thenReturn(Optional.of(entity));

        userDetailDomainRepository.updateThemePreference(5L, ThemePreference.DARK);

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getThemePreference()).isEqualTo(ThemePreference.DARK);
    }

    @Test
    void updateThemePreference_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(105L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.updateThemePreference(105L, ThemePreference.DARK))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findDailyClaimState_shouldReturnStreakAndDate_whenPresent() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(1L).dailyRewardStreak(5).lastDailyClaimDate(LocalDate.of(2026, 6, 12)).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(1L);

        assertThat(state.streak()).isEqualTo(5);
        assertThat(state.lastClaimDate()).isEqualTo(LocalDate.of(2026, 6, 12));
    }

    @Test
    void findDailyClaimState_shouldDefaultStreakToZero_whenStreakIsNull() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(1L).dailyRewardStreak(null).lastDailyClaimDate(null).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        DailyClaimState state = userDetailDomainRepository.findDailyClaimState(1L);

        assertThat(state.streak()).isEqualTo(0);
        assertThat(state.lastClaimDate()).isNull();
    }

    @Test
    void findDailyClaimState_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.findDailyClaimState(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateDailyClaim_shouldPersistStreakAndDate() {
        UserDetailEntity entity = UserDetailEntity.builder().id(1L).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        userDetailDomainRepository.updateDailyClaim(1L, 4, LocalDate.of(2026, 6, 12));

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getDailyRewardStreak()).isEqualTo(4);
        assertThat(captor.getValue().getLastDailyClaimDate()).isEqualTo(LocalDate.of(2026, 6, 12));
    }

    @Test
    void updateDailyClaim_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.updateDailyClaim(99L, 1, LocalDate.of(2026, 6, 12)))
                .isInstanceOf(NotFoundException.class);
    }

}
