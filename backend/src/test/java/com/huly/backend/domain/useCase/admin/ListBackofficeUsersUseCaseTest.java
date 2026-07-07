package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.emotionalRecommendation.EmotionalEvent;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.model.user.UserPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListBackofficeUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAntiScrollSettingsRepository settingsRepository;

    @Mock
    private ExtensionMetricsRepository metricsRepository;

    @Mock
    private UserPlanRepository userPlanRepository;

    @Mock
    private EmotionalEventRepository emotionalEventRepository;

    @InjectMocks
    private ListBackofficeUsersUseCase useCase;

    @Test
    @DisplayName("Procesa usuarios y configuración sin consultar las métricas pesadas")
    void executeShouldProcessUsersAndSettingsWithoutHeavyMetrics() {
        // --- arrange ---
        givenNonAdmins(johnDoe());
        givenSettings(2L, settings(true, true));
        givenCoins(2L, 50);
        givenNoPlan(2L);
        givenEmotionalEvents(2L);

        // --- act ---
        List<BackofficeUserSummary> result = list();

        // --- assert ---
        thenJohnDoeSummary(result);
    }

    @Test
    @DisplayName("Filtra por nombre sin distinguir mayúsculas")
    void executeWithSearchShouldFilterByNameCaseInsensitive() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Alice Smith", "alice@gmail.com"), user(2L, "Bob Jones", "bob@huly.com"));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoPlanForAllUsers();
        givenNoEmotionalEventsForAllUsers();

        // --- act ---
        List<BackofficeUserSummary> result = listMatching("alice");

        // --- assert ---
        thenSingleUserNamed(result, "Alice Smith");
    }

    @Test
    @DisplayName("Filtra por email sin distinguir mayúsculas")
    void executeWithSearchShouldFilterByEmailCaseInsensitive() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Alice Smith", "alice@gmail.com"), user(2L, "Bob Jones", "bob@huly.com"));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoPlanForAllUsers();
        givenNoEmotionalEventsForAllUsers();

        // --- act ---
        List<BackofficeUserSummary> result = listMatching("HULY");

        // --- assert ---
        thenSingleUserWithEmail(result, "bob@huly.com");
    }

    @Test
    @DisplayName("Devuelve vacío cuando la búsqueda no coincide con ningún usuario")
    void executeWithSearchShouldReturnEmptyWhenNoMatch() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Alice Smith", "alice@gmail.com"), user(2L, "Bob Jones", "bob@huly.com"));

        // --- act ---
        List<BackofficeUserSummary> result = listMatching("nonexistent");

        // --- assert ---
        thenEmptyResult(result);
    }

    @Test
    @DisplayName("Devuelve todos los usuarios cuando la búsqueda es null")
    void executeWithSearchShouldReturnAllWhenSearchIsNull() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Alice Smith", "alice@gmail.com"), user(2L, "Bob Jones", "bob@huly.com"));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoPlanForAllUsers();
        givenNoEmotionalEventsForAllUsers();

        // --- act ---
        List<BackofficeUserSummary> result = listMatching(null);

        // --- assert ---
        thenResultSize(result, 2);
    }

    @Test
    @DisplayName("Devuelve todos los usuarios cuando la búsqueda está en blanco")
    void executeWithSearchShouldReturnAllWhenSearchIsBlank() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Alice Smith", "alice@gmail.com"), user(2L, "Bob Jones", "bob@huly.com"));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoPlanForAllUsers();
        givenNoEmotionalEventsForAllUsers();

        // --- act ---
        List<BackofficeUserSummary> result = listMatching("   ");

        // --- assert ---
        thenResultSize(result, 2);
    }

    @Test
    @DisplayName("Determina la emoción dominante por frecuencia ignorando emociones nulas")
    void executeShouldDetermineDominantEmotionByFrequency() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Test User", "test@example.com"));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoPlanForAllUsers();
        givenEmotionalEvents(1L, emotion("Happy"), emotion("Sad"), emotion("Happy"), emotion(null));

        // --- act ---
        List<BackofficeUserSummary> result = list();

        // --- assert ---
        thenDominantEmotion(result, "HAPPY");
    }

    @Test
    @DisplayName("Usa NEUTRAL como emoción dominante cuando no hay emociones detectadas")
    void executeShouldDefaultToNeutralWhenNoEmotionalEventsWithEmotion() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Test User", "test@example.com"));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoPlanForAllUsers();
        givenEmotionalEvents(1L, emotion(null));

        // --- act ---
        List<BackofficeUserSummary> result = list();

        // --- assert ---
        thenDominantEmotion(result, "NEUTRAL");
    }

    @Test
    @DisplayName("No lanza NPE cuando el usuario tiene nombre y email nulos")
    void executeWithSearchShouldNotThrowNpeWhenUserFieldsAreNull() {
        // --- arrange ---
        givenNonAdmins(user(1L, null, null));

        // --- act ---
        List<BackofficeUserSummary> result = listMatching("test");

        // --- assert ---
        thenEmptyResult(result);
    }

    @Test
    @DisplayName("Resuelve el plan del usuario según esté activo o vencido")
    void executeShouldDetermineUserPlanWhenActiveOrInactive() {
        // --- arrange ---
        givenNonAdmins(user(1L, "Active User", null), user(2L, "Inactive User", null));
        givenNoSettingsForAllUsers();
        givenZeroCoinsForAllUsers();
        givenNoEmotionalEventsForAllUsers();
        givenPlan(1L, plan("Premium", Instant.now().plusSeconds(86400)));
        givenPlan(2L, plan("Pro", Instant.now().minusSeconds(86400)));

        // --- act ---
        List<BackofficeUserSummary> result = list();

        // --- assert ---
        thenPlans(result, "Premium", "Gratuito");
    }

    // --- arrange ---

    private void givenNonAdmins(AppUser... users) {
        when(userRepository.findAllNonAdmins()).thenReturn(List.of(users));
    }

    private void givenSettings(Long userId, UserAntiScrollSettings settings) {
        when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));
    }

    private void givenNoSettingsForAllUsers() {
        when(settingsRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
    }

    private void givenCoins(Long userId, int coins) {
        when(userRepository.getCoins(userId)).thenReturn(coins);
    }

    private void givenZeroCoinsForAllUsers() {
        when(userRepository.getCoins(anyLong())).thenReturn(0);
    }

    private void givenNoPlan(Long userId) {
        when(userPlanRepository.findByUser(userId)).thenReturn(Optional.empty());
    }

    private void givenNoPlanForAllUsers() {
        when(userPlanRepository.findByUser(anyLong())).thenReturn(Optional.empty());
    }

    private void givenPlan(Long userId, UserPlan plan) {
        when(userPlanRepository.findByUser(userId)).thenReturn(Optional.of(plan));
    }

    private void givenEmotionalEvents(Long userId, EmotionalEvent... events) {
        when(emotionalEventRepository.findByUserId(userId)).thenReturn(List.of(events));
    }

    private void givenNoEmotionalEventsForAllUsers() {
        when(emotionalEventRepository.findByUserId(anyLong())).thenReturn(List.of());
    }

    private AppUser johnDoe() {
        return AppUser.builder()
                .id(2L)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();
    }

    private AppUser user(Long id, String name, String email) {
        return AppUser.builder().id(id).name(name).email(email).build();
    }

    private UserAntiScrollSettings settings(boolean enabled, boolean consent) {
        return UserAntiScrollSettings.builder().enabled(enabled).dataSharingConsent(consent).build();
    }

    private UserPlan plan(String planCode, Instant expiresAt) {
        return UserPlan.builder().planCode(planCode).expiresAt(expiresAt).build();
    }

    private EmotionalEvent emotion(String detectedEmotion) {
        return EmotionalEvent.builder().detectedEmotion(detectedEmotion).build();
    }

    // --- act ---

    private List<BackofficeUserSummary> list() {
        return useCase.execute();
    }

    private List<BackofficeUserSummary> listMatching(String search) {
        return useCase.execute(search);
    }

    // --- assert ---

    private void thenJohnDoeSummary(List<BackofficeUserSummary> result) {
        assertThat(result).hasSize(1);
        BackofficeUserSummary summary = result.get(0);
        assertThat(summary.getId()).isEqualTo(2L);
        assertThat(summary.getName()).isEqualTo("John Doe");
        assertThat(summary.isAntiScrollEnabled()).isTrue();
        assertThat(summary.isDataSharingConsent()).isTrue();
        assertThat(summary.getCoins()).isEqualTo(50);
        assertThat(summary.getPlan()).isEqualTo("Gratuito");
        assertThat(summary.getMostUsedApp()).isNull();
        assertThat(summary.getMostUsedAppActiveSeconds()).isEqualTo(0);
        assertThat(summary.getTotalScrollTimeSeconds()).isEqualTo(0);
        assertThat(summary.getTopApps()).isEmpty();
        assertThat(summary.getDailyScrollTimeSeconds()).isEmpty();
        verifyNoInteractions(metricsRepository);
    }

    private void thenSingleUserNamed(List<BackofficeUserSummary> result, String name) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(name);
    }

    private void thenSingleUserWithEmail(List<BackofficeUserSummary> result, String email) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo(email);
    }

    private void thenEmptyResult(List<BackofficeUserSummary> result) {
        assertThat(result).isEmpty();
    }

    private void thenResultSize(List<BackofficeUserSummary> result, int size) {
        assertThat(result).hasSize(size);
    }

    private void thenDominantEmotion(List<BackofficeUserSummary> result, String emotion) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDominantEmotion()).isEqualTo(emotion);
    }

    private void thenPlans(List<BackofficeUserSummary> result, String firstPlan, String secondPlan) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlan()).isEqualTo(firstPlan);
        assertThat(result.get(1).getPlan()).isEqualTo(secondPlan);
    }
}
