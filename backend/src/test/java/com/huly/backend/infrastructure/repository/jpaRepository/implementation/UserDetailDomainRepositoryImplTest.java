package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.dailyReward.DailyClaimState;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.user.AudioSettings;
import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailDomainRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@test.com";
    private static final String NAME = "Maxi";
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 5, 20);
    private static final LocalDate CLAIM_DATE = LocalDate.of(2026, 6, 12);
    private static final LocalDate LOGIN_DATE = LocalDate.of(2026, 7, 6);
    private static final String NEW_NAME = "Nuevo Nombre";
    private static final LocalDate NEW_BIRTH = LocalDate.of(1999, 12, 31);
    private static final String ANSWER_1 = "Calmar mi mente";
    private static final String ANSWER_2 = "Soltar el control";
    private static final String ANSWER_3 = "Respirar antes de reaccionar";

    @Mock
    private UserDetailRepository userDetailRepository;

    @InjectMocks
    private UserDetailDomainRepositoryImpl userDetailDomainRepository;

    @Test
    @DisplayName("Devuelve el estado de onboarding cuando existe el detalle del usuario")
    void findOnBoardingCompletedShouldReturnValueWhenUserDetailExists() {
        givenUserDetailFound(onBoardingCompletedEntity());

        Optional<Boolean> result = findOnBoardingCompleted();

        thenBooleanIsTrue(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar el estado de onboarding cuando no existe el detalle")
    void findOnBoardingCompletedShouldReturnEmptyWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        Optional<Boolean> result = findOnBoardingCompleted();

        thenBooleanIsEmpty(result);
    }

    @Test
    @DisplayName("Devuelve el estado del tutorial de onboarding cuando existe el detalle del usuario")
    void findOnboardingTutorialCompletedShouldReturnValueWhenUserDetailExists() {
        givenUserDetailFound(onboardingTutorialCompletedEntity());

        Optional<Boolean> result = findOnboardingTutorialCompleted();

        thenBooleanIsTrue(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar el estado del tutorial de onboarding cuando no existe el detalle")
    void findOnboardingTutorialCompletedShouldReturnEmptyWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        Optional<Boolean> result = findOnboardingTutorialCompleted();

        thenBooleanIsEmpty(result);
    }

    @Test
    @DisplayName("Devuelve el estado del tutorial de perfil cuando existe el detalle del usuario")
    void findProfileOnboardingTutorialCompletedShouldReturnValueWhenUserDetailExists() {
        givenUserDetailFound(profileTutorialCompletedEntity());

        Optional<Boolean> result = findProfileOnboardingTutorialCompleted();

        thenBooleanIsTrue(result);
    }

    @Test
    @DisplayName("Devuelve la preferencia de tema cuando existe el detalle del usuario")
    void findThemePreferenceShouldReturnValueWhenUserDetailExists() {
        givenUserDetailFound(themeEntity(ThemePreference.DARK));

        ThemePreference result = findThemePreference();

        thenThemeIs(result, ThemePreference.DARK);
    }

    @Test
    @DisplayName("Falla al buscar la preferencia de tema cuando no existe el detalle del usuario")
    void findThemePreferenceShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenFindThemePreferenceThrowsNotFound();
    }

    @Test
    @DisplayName("Devuelve los volúmenes persistidos cuando existe el detalle del usuario")
    void findAudioSettingsShouldReturnPersistedVolumesWhenUserDetailExists() {
        givenUserDetailFound(audioEntity(0.25, 0.45, 0.85));

        AudioSettings result = findAudioSettings();

        thenAudioVolumesAre(result, 0.25, 0.45, 0.85);
    }

    @Test
    @DisplayName("Devuelve los volúmenes por defecto cuando los volúmenes son nulos")
    void findAudioSettingsShouldReturnDefaultsWhenVolumesAreNull() {
        givenUserDetailFound(audioEntity(null, null, null));

        AudioSettings result = findAudioSettings();

        thenAudioVolumesAre(result, 0.7, 0.1, 1.0);
    }

    @Test
    @DisplayName("Falla al buscar los volúmenes de audio cuando no existe el detalle del usuario")
    void findAudioSettingsShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenFindAudioSettingsThrowsNotFound();
    }

    @Test
    @DisplayName("Guarda las respuestas y marca el onboarding como completado")
    void completeOnboardingShouldSetAnswersAndMarkCompleted() {
        givenUserDetailFound(plainEntity());

        completeOnboarding();

        thenOnboardingAnswersPersistedAndCompleted();
    }

    @Test
    @DisplayName("Falla al completar el onboarding cuando no existe el detalle del usuario")
    void completeOnboardingShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenCompleteOnboardingThrowsNotFound();
    }

    @Test
    @DisplayName("Marca el tutorial de onboarding como completado")
    void completeTutorialShouldMarkTutorialCompleted() {
        givenUserDetailFound(plainEntity());

        completeTutorial();

        thenTutorialMarkedCompleted();
    }

    @Test
    @DisplayName("Falla al completar el tutorial cuando no existe el detalle del usuario")
    void completeTutorialShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenCompleteTutorialThrowsNotFound();
    }

    @Test
    @DisplayName("Marca el tutorial de perfil como completado")
    void completeProfileTutorialShouldMarkProfileTutorialCompleted() {
        givenUserDetailFound(plainEntity());

        completeProfileTutorial();

        thenProfileTutorialMarkedCompleted();
    }

    @Test
    @DisplayName("Falla al completar el tutorial de perfil cuando no existe el detalle del usuario")
    void completeProfileTutorialShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenCompleteProfileTutorialThrowsNotFound();
    }

    @Test
    @DisplayName("Persiste la preferencia de tema cuando existe el detalle del usuario")
    void updateThemePreferenceShouldPersistThemePreference() {
        givenUserDetailFound(themeEntity(ThemePreference.LIGHT));

        updateThemePreference(ThemePreference.DARK);

        thenThemePersisted(ThemePreference.DARK);
    }

    @Test
    @DisplayName("Falla al actualizar la preferencia de tema cuando no existe el detalle del usuario")
    void updateThemePreferenceShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenUpdateThemePreferenceThrowsNotFound();
    }

    @Test
    @DisplayName("Persiste y devuelve los volúmenes de audio")
    void updateAudioSettingsShouldPersistVolumes() {
        givenUserDetailFound(plainEntity());
        givenSaveReturnsSameEntity();

        AudioSettings result = updateAudioSettings(0.2, 0.4, 0.6);

        thenAudioSettingsPersistedAndReturned(result, 0.2, 0.4, 0.6);
    }

    @Test
    @DisplayName("Acota los volúmenes de audio antes de persistir")
    void updateAudioSettingsShouldClampVolumesBeforePersisting() {
        givenUserDetailFound(plainEntity());
        givenSaveReturnsSameEntity();

        AudioSettings result = updateAudioSettings(-1.0, 2.0, null);

        thenAudioVolumesAre(result, 0.0, 1.0, 0.0);
    }

    @Test
    @DisplayName("Falla al actualizar los volúmenes de audio cuando no existe el detalle del usuario")
    void updateAudioSettingsShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenUpdateAudioSettingsThrowsNotFound();
    }

    @Test
    @DisplayName("Devuelve la racha y la fecha del reclamo diario cuando están presentes")
    void findDailyClaimStateShouldReturnStreakAndDateWhenPresent() {
        givenUserDetailFound(dailyClaimEntity(5, CLAIM_DATE));

        DailyClaimState result = findDailyClaimState();

        thenDailyClaimStateIs(result, 5, CLAIM_DATE);
    }

    @Test
    @DisplayName("Usa racha cero por defecto cuando la racha es nula")
    void findDailyClaimStateShouldDefaultStreakToZeroWhenStreakIsNull() {
        givenUserDetailFound(dailyClaimEntity(null, null));

        DailyClaimState result = findDailyClaimState();

        thenDailyClaimStateIs(result, 0, null);
    }

    @Test
    @DisplayName("Falla al buscar el estado del reclamo diario cuando no existe el detalle del usuario")
    void findDailyClaimStateShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenFindDailyClaimStateThrowsNotFound();
    }

    @Test
    @DisplayName("Persiste la racha y la fecha del reclamo diario")
    void updateDailyClaimShouldPersistStreakAndDate() {
        givenUserDetailFound(plainEntity());

        updateDailyClaim(4, CLAIM_DATE);

        thenDailyClaimPersisted(4, CLAIM_DATE);
    }

    @Test
    @DisplayName("Falla al actualizar el reclamo diario cuando no existe el detalle del usuario")
    void updateDailyClaimShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenUpdateDailyClaimThrowsNotFound();
    }

    @Test
    @DisplayName("Devuelve la configuración de cuenta mapeada cuando existe el detalle del usuario")
    void findAccountSettingsShouldReturnMappedSettingsWhenUserDetailExists() {
        givenUserDetailFound(accountSettingsEntity());

        UserAccountSettings result = findAccountSettings();

        thenAccountSettingsAre(result, NAME, EMAIL, BIRTH_DATE);
    }

    @Test
    @DisplayName("Falla al buscar la configuración de cuenta cuando no existe el detalle del usuario")
    void findAccountSettingsShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenFindAccountSettingsThrowsNotFound();
    }

    @Test
    @DisplayName("Persiste el nombre y la fecha de nacimiento y devuelve la configuración de cuenta mapeada")
    void updateAccountSettingsShouldPersistNameAndBirthAndReturnMappedSettings() {
        givenUserDetailFound(entityWithAppUser());
        givenSaveReturnsSameEntity();

        UserAccountSettings result = updateAccountSettings(NEW_NAME, NEW_BIRTH);

        thenAccountSettingsPersistedAndReturned(result);
    }

    @Test
    @DisplayName("Falla al actualizar la configuración de cuenta cuando no existe el detalle del usuario")
    void updateAccountSettingsShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenUpdateAccountSettingsThrowsNotFound();
    }

    @Test
    @DisplayName("Devuelve la fecha del último ingreso cuando está presente")
    void findLastLoginDateShouldReturnDateWhenPresent() {
        givenUserDetailFound(lastLoginEntity());

        Optional<LocalDate> result = findLastLoginDate();

        thenLastLoginDateIs(result, LOGIN_DATE);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar la fecha del último ingreso cuando no existe el detalle del usuario")
    void findLastLoginDateShouldReturnEmptyWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        Optional<LocalDate> result = findLastLoginDate();

        thenLastLoginDateIsEmpty(result);
    }

    @Test
    @DisplayName("Persiste la fecha del último ingreso")
    void updateLastLoginDateShouldPersistDate() {
        givenUserDetailFound(plainEntity());

        updateLastLoginDate(LOGIN_DATE);

        thenLastLoginDatePersisted(LOGIN_DATE);
    }

    @Test
    @DisplayName("Falla al actualizar la fecha del último ingreso cuando no existe el detalle del usuario")
    void updateLastLoginDateShouldThrowNotFoundExceptionWhenUserDetailNotFound() {
        givenUserDetailNotFound();

        thenUpdateLastLoginDateThrowsNotFound();
    }

    // --- arrange ---
    private void givenUserDetailFound(UserDetailEntity entity) {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(Optional.of(entity));
    }

    private void givenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(Optional.empty());
    }

    private void givenSaveReturnsSameEntity() {
        when(userDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private UserDetailEntity plainEntity() {
        return UserDetailEntity.builder().id(USER_ID).build();
    }

    private UserDetailEntity onBoardingCompletedEntity() {
        return UserDetailEntity.builder().id(USER_ID).onBoardingCompleted(true).build();
    }

    private UserDetailEntity onboardingTutorialCompletedEntity() {
        return UserDetailEntity.builder().id(USER_ID).onboardingTutorialCompleted(true).build();
    }

    private UserDetailEntity profileTutorialCompletedEntity() {
        return UserDetailEntity.builder().id(USER_ID).profileOnboardingTutorialCompleted(true).build();
    }

    private UserDetailEntity themeEntity(ThemePreference theme) {
        return UserDetailEntity.builder().id(USER_ID).themePreference(theme).build();
    }

    private UserDetailEntity audioEntity(Double interfaceVolume, Double ambientVolume, Double minigameVolume) {
        return UserDetailEntity.builder()
                .id(USER_ID)
                .interfaceVolume(interfaceVolume)
                .ambientVolume(ambientVolume)
                .minigameVolume(minigameVolume)
                .build();
    }

    private UserDetailEntity dailyClaimEntity(Integer streak, LocalDate lastClaimDate) {
        return UserDetailEntity.builder()
                .id(USER_ID)
                .dailyRewardStreak(streak)
                .lastDailyClaimDate(lastClaimDate)
                .build();
    }

    private UserDetailEntity accountSettingsEntity() {
        return UserDetailEntity.builder()
                .id(USER_ID)
                .appUser(appUserEntity())
                .name(NAME)
                .birth(BIRTH_DATE)
                .build();
    }

    private UserDetailEntity entityWithAppUser() {
        return UserDetailEntity.builder().id(USER_ID).appUser(appUserEntity()).build();
    }

    private UserDetailEntity lastLoginEntity() {
        return UserDetailEntity.builder().id(USER_ID).lastLoginDate(LOGIN_DATE).build();
    }

    private AppUserEntity appUserEntity() {
        return AppUserEntity.builder().id(1L).email(EMAIL).build();
    }

    // --- act ---
    private Optional<Boolean> findOnBoardingCompleted() {
        return userDetailDomainRepository.findOnBoardingCompleted(USER_ID);
    }

    private Optional<Boolean> findOnboardingTutorialCompleted() {
        return userDetailDomainRepository.findOnboardingTutorialCompleted(USER_ID);
    }

    private Optional<Boolean> findProfileOnboardingTutorialCompleted() {
        return userDetailDomainRepository.findProfileOnboardingTutorialCompleted(USER_ID);
    }

    private ThemePreference findThemePreference() {
        return userDetailDomainRepository.findThemePreference(USER_ID);
    }

    private AudioSettings findAudioSettings() {
        return userDetailDomainRepository.findAudioSettings(USER_ID);
    }

    private UserAccountSettings findAccountSettings() {
        return userDetailDomainRepository.findAccountSettings(USER_ID);
    }

    private void completeOnboarding() {
        userDetailDomainRepository.completeOnboarding(USER_ID, ANSWER_1, ANSWER_2, ANSWER_3);
    }

    private void completeTutorial() {
        userDetailDomainRepository.completeTutorial(USER_ID);
    }

    private void completeProfileTutorial() {
        userDetailDomainRepository.completeProfileTutorial(USER_ID);
    }

    private void updateThemePreference(ThemePreference theme) {
        userDetailDomainRepository.updateThemePreference(USER_ID, theme);
    }

    private AudioSettings updateAudioSettings(Double interfaceVolume, Double ambientVolume, Double minigameVolume) {
        return userDetailDomainRepository.updateAudioSettings(
                USER_ID, new AudioSettings(interfaceVolume, ambientVolume, minigameVolume));
    }

    private UserAccountSettings updateAccountSettings(String name, LocalDate birthDate) {
        return userDetailDomainRepository.updateAccountSettings(
                USER_ID, new UserAccountSettings(name, EMAIL, birthDate));
    }

    private DailyClaimState findDailyClaimState() {
        return userDetailDomainRepository.findDailyClaimState(USER_ID);
    }

    private void updateDailyClaim(int streak, LocalDate claimDate) {
        userDetailDomainRepository.updateDailyClaim(USER_ID, streak, claimDate);
    }

    private Optional<LocalDate> findLastLoginDate() {
        return userDetailDomainRepository.findLastLoginDate(USER_ID);
    }

    private void updateLastLoginDate(LocalDate date) {
        userDetailDomainRepository.updateLastLoginDate(USER_ID, date);
    }

    // --- assert ---
    private UserDetailEntity savedEntity() {
        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        return captor.getValue();
    }

    private void thenBooleanIsTrue(Optional<Boolean> result) {
        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }

    private void thenBooleanIsEmpty(Optional<Boolean> result) {
        assertThat(result).isEmpty();
    }

    private void thenThemeIs(ThemePreference result, ThemePreference expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenAudioVolumesAre(AudioSettings result, double interfaceVolume, double ambientVolume, double minigameVolume) {
        assertThat(result.interfaceVolume()).isEqualTo(interfaceVolume);
        assertThat(result.ambientVolume()).isEqualTo(ambientVolume);
        assertThat(result.minigameVolume()).isEqualTo(minigameVolume);
    }

    private void thenAudioSettingsPersistedAndReturned(AudioSettings result, double interfaceVolume, double ambientVolume, double minigameVolume) {
        UserDetailEntity saved = savedEntity();
        assertThat(saved.getInterfaceVolume()).isEqualTo(interfaceVolume);
        assertThat(saved.getAmbientVolume()).isEqualTo(ambientVolume);
        assertThat(saved.getMinigameVolume()).isEqualTo(minigameVolume);
        thenAudioVolumesAre(result, interfaceVolume, ambientVolume, minigameVolume);
    }

    private void thenOnboardingAnswersPersistedAndCompleted() {
        UserDetailEntity saved = savedEntity();
        assertThat(saved.getOnboardingAnswer1()).isEqualTo(ANSWER_1);
        assertThat(saved.getOnboardingAnswer2()).isEqualTo(ANSWER_2);
        assertThat(saved.getOnboardingAnswer3()).isEqualTo(ANSWER_3);
        assertThat(saved.getOnBoardingCompleted()).isTrue();
        assertThat(saved.getProfileOnBoardingCompleted()).isNull();
    }

    private void thenTutorialMarkedCompleted() {
        assertThat(savedEntity().getOnboardingTutorialCompleted()).isTrue();
    }

    private void thenProfileTutorialMarkedCompleted() {
        assertThat(savedEntity().getProfileOnboardingTutorialCompleted()).isTrue();
    }

    private void thenThemePersisted(ThemePreference expected) {
        assertThat(savedEntity().getThemePreference()).isEqualTo(expected);
    }

    private void thenDailyClaimStateIs(DailyClaimState result, int expectedStreak, LocalDate expectedDate) {
        assertThat(result.streak()).isEqualTo(expectedStreak);
        assertThat(result.lastClaimDate()).isEqualTo(expectedDate);
    }

    private void thenDailyClaimPersisted(int expectedStreak, LocalDate expectedDate) {
        UserDetailEntity saved = savedEntity();
        assertThat(saved.getDailyRewardStreak()).isEqualTo(expectedStreak);
        assertThat(saved.getLastDailyClaimDate()).isEqualTo(expectedDate);
    }

    private void thenAccountSettingsAre(UserAccountSettings result, String expectedName, String expectedEmail, LocalDate expectedBirthDate) {
        assertThat(result.name()).isEqualTo(expectedName);
        assertThat(result.email()).isEqualTo(expectedEmail);
        assertThat(result.birthDate()).isEqualTo(expectedBirthDate);
    }

    private void thenAccountSettingsPersistedAndReturned(UserAccountSettings result) {
        UserDetailEntity saved = savedEntity();
        assertThat(saved.getName()).isEqualTo(NEW_NAME);
        assertThat(saved.getBirth()).isEqualTo(NEW_BIRTH);
        thenAccountSettingsAre(result, NEW_NAME, EMAIL, NEW_BIRTH);
    }

    private void thenLastLoginDateIs(Optional<LocalDate> result, LocalDate expected) {
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
    }

    private void thenLastLoginDateIsEmpty(Optional<LocalDate> result) {
        assertThat(result).isEmpty();
    }

    private void thenLastLoginDatePersisted(LocalDate expected) {
        assertThat(savedEntity().getLastLoginDate()).isEqualTo(expected);
    }

    private void thenFindThemePreferenceThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.findThemePreference(USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenCompleteOnboardingThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.completeOnboarding(USER_ID, ANSWER_1, ANSWER_2, ANSWER_3))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenCompleteTutorialThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.completeTutorial(USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenFindAudioSettingsThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.findAudioSettings(USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenCompleteProfileTutorialThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.completeProfileTutorial(USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenUpdateThemePreferenceThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.updateThemePreference(USER_ID, ThemePreference.DARK))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenUpdateAudioSettingsThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.updateAudioSettings(USER_ID, new AudioSettings(0.2, 0.4, 0.6)))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenFindDailyClaimStateThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.findDailyClaimState(USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenUpdateDailyClaimThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.updateDailyClaim(USER_ID, 1, CLAIM_DATE))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenFindAccountSettingsThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.findAccountSettings(USER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenUpdateAccountSettingsThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.updateAccountSettings(
                USER_ID, new UserAccountSettings(NEW_NAME, EMAIL, NEW_BIRTH)))
                .isInstanceOf(NotFoundException.class);
    }

    private void thenUpdateLastLoginDateThrowsNotFound() {
        assertThatThrownBy(() -> userDetailDomainRepository.updateLastLoginDate(USER_ID, LOGIN_DATE))
                .isInstanceOf(NotFoundException.class);
    }
}
