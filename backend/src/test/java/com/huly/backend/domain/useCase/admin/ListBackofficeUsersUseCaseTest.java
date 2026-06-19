package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.model.admin.BackofficeUserSummary;
import com.huly.backend.domain.model.extension.UserAntiScrollSettings;
import com.huly.backend.domain.repository.chatBotConfig.EmotionalEventRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.repository.extension.ExtensionMetricsRepository;
import com.huly.backend.domain.repository.extension.UserAntiScrollSettingsRepository;
import com.huly.backend.domain.repository.user.UserPlanRepository;
import com.huly.backend.domain.dto.payment.UserPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ListBackofficeUsersUseCaseTest {

    private UserRepository userRepository;
    private UserAntiScrollSettingsRepository settingsRepository;
    private ExtensionMetricsRepository metricsRepository;
    private UserPlanRepository userPlanRepository;
    private EmotionalEventRepository emotionalEventRepository;
    private ListBackofficeUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        settingsRepository = mock(UserAntiScrollSettingsRepository.class);
        metricsRepository = mock(ExtensionMetricsRepository.class);
        userPlanRepository = mock(UserPlanRepository.class);
        emotionalEventRepository = mock(EmotionalEventRepository.class);
        useCase = new ListBackofficeUsersUseCase(userRepository, settingsRepository, metricsRepository, userPlanRepository, emotionalEventRepository);
    }

    @Test
    void execute_shouldProcessUsersAndSettingsWithoutHeavyMetrics() {
        AppUser user = AppUser.builder()
                .id(2L)
                .name("John Doe")
                .email("john@example.com")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        UserAntiScrollSettings settings = UserAntiScrollSettings.builder()
                .enabled(true)
                .dataSharingConsent(true)
                .build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(2L)).thenReturn(Optional.of(settings));
        when(userRepository.getCoins(2L)).thenReturn(50);
        when(userPlanRepository.findByUser(2L)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(2L)).thenReturn(List.of());

        List<BackofficeUserSummary> result = useCase.execute();

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

    @Test
    void executeWithSearch_shouldFilterByNameAndEmailCaseInsensitive() {
        AppUser user1 = AppUser.builder().id(1L).name("Alice Smith").email("alice@gmail.com").build();
        AppUser user2 = AppUser.builder().id(2L).name("Bob Jones").email("bob@huly.com").build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user1, user2));
        when(settingsRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(userRepository.getCoins(anyLong())).thenReturn(0);
        when(userPlanRepository.findByUser(anyLong())).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(anyLong())).thenReturn(List.of());

        List<BackofficeUserSummary> searchByName = useCase.execute("alice");
        assertThat(searchByName).hasSize(1);
        assertThat(searchByName.get(0).getName()).isEqualTo("Alice Smith");

        List<BackofficeUserSummary> searchByEmail = useCase.execute("HULY");
        assertThat(searchByEmail).hasSize(1);
        assertThat(searchByEmail.get(0).getEmail()).isEqualTo("bob@huly.com");

        List<BackofficeUserSummary> searchNoResults = useCase.execute("nonexistent");
        assertThat(searchNoResults).isEmpty();

        List<BackofficeUserSummary> searchNull = useCase.execute(null);
        assertThat(searchNull).hasSize(2);

        List<BackofficeUserSummary> searchBlank = useCase.execute("   ");
        assertThat(searchBlank).hasSize(2);
    }

    @Test
    void execute_shouldDetermineDominantEmotionCorrectly() {
        AppUser user = AppUser.builder().id(1L).name("Test User").email("test@example.com").build();

        EmotionalEvent ev1 = EmotionalEvent.builder().detectedEmotion("Happy").build();
        EmotionalEvent ev2 = EmotionalEvent.builder().detectedEmotion("Sad").build();
        EmotionalEvent ev3 = EmotionalEvent.builder().detectedEmotion("Happy").build();
        EmotionalEvent evNull = EmotionalEvent.builder().detectedEmotion(null).build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.getCoins(1L)).thenReturn(0);
        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(1L)).thenReturn(List.of(ev1, ev2, ev3, evNull));

        List<BackofficeUserSummary> result = useCase.execute();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDominantEmotion()).isEqualTo("HAPPY");
    }

    @Test
    void execute_shouldDefaultToNeutral_whenNoEmotionalEventsWithEmotion() {
        AppUser user = AppUser.builder().id(1L).name("Test User").email("test@example.com").build();
        EmotionalEvent evNull = EmotionalEvent.builder().detectedEmotion(null).build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.getCoins(1L)).thenReturn(0);
        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(1L)).thenReturn(List.of(evNull));

        List<BackofficeUserSummary> result = useCase.execute();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDominantEmotion()).isEqualTo("NEUTRAL");
    }

    @Test
    void executeWithSearch_shouldNotThrowNpe_whenUserFieldsAreNull() {
        AppUser user = AppUser.builder().id(1L).name(null).email(null).build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(user));
        when(settingsRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(userRepository.getCoins(anyLong())).thenReturn(0);
        when(userPlanRepository.findByUser(anyLong())).thenReturn(Optional.empty());
        when(emotionalEventRepository.findByUserId(anyLong())).thenReturn(List.of());

        List<BackofficeUserSummary> result = useCase.execute("test");
        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldDetermineUserPlanCorrectly_whenUserHasActiveOrInactivePlan() {
        AppUser userActive = AppUser.builder().id(1L).name("Active User").build();
        AppUser userInactive = AppUser.builder().id(2L).name("Inactive User").build();

        UserPlan activePlan = UserPlan.builder()
                .planCode("Premium")
                .expiresAt(java.time.Instant.now().plusSeconds(86400))
                .build();
        UserPlan inactivePlan = UserPlan.builder()
                .planCode("Pro")
                .expiresAt(java.time.Instant.now().minusSeconds(86400))
                .build();

        when(userRepository.findAllNonAdmins()).thenReturn(List.of(userActive, userInactive));
        when(settingsRepository.findByUserId(anyLong())).thenReturn(Optional.empty());
        when(userRepository.getCoins(anyLong())).thenReturn(0);
        when(emotionalEventRepository.findByUserId(anyLong())).thenReturn(List.of());

        when(userPlanRepository.findByUser(1L)).thenReturn(Optional.of(activePlan));
        when(userPlanRepository.findByUser(2L)).thenReturn(Optional.of(inactivePlan));

        List<BackofficeUserSummary> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlan()).isEqualTo("Premium");
        assertThat(result.get(1).getPlan()).isEqualTo("Gratuito");
    }
}

