package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesRequest;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesResponse;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserActivitiesUseCaseTest {

    private static final Long USER_ID = 1L;

    private UserRepository userRepository;
    private ActivitySessionRepository activitySessionRepository;
    private GetUserActivitiesUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        activitySessionRepository = mock(ActivitySessionRepository.class);
        useCase = new GetUserActivitiesUseCase(userRepository, activitySessionRepository);
    }

    @Test
    void execute_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TOTAL)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void execute_shouldUseDatabaseFilteringAndBuildResponse() {
        Instant now = Instant.now();
        ActivitySession session = ActivitySession.builder()
                .id(10L)
                .userId(USER_ID)
                .activityType(ActivityType.BREATHING)
                .createdAt(now)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(session));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(session));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TODAY));

        assertThat(response.todayActivitiesCount()).isEqualTo(1);
        assertThat(response.favoriteActivity()).isEqualTo("BREATHING");
        assertThat(response.averageSessionsText()).isEqualTo("1 sesión hoy");
        assertThat(response.activityDistribution()).containsEntry("BREATHING", 1);
        assertThat(response.activitySessions()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(10L);
            assertThat(item.activityType()).isEqualTo("BREATHING");
            assertThat(item.createdAt()).isEqualTo(now);
        });
    }

    @Test
    void execute_shouldCalculateWeeklyAverageForTotalTimeframe() {
        Instant now = Instant.now();
        Instant tenDaysAgo = now.minus(10, ChronoUnit.DAYS);
        ActivitySession oldSession = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(tenDaysAgo)
                .build();
        ActivitySession recentSession = ActivitySession.builder()
                .id(2L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(now)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserId(USER_ID)).thenReturn(List.of(oldSession, recentSession));
        when(activitySessionRepository.countByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(1L);
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.of(oldSession));
        when(activitySessionRepository.findRecentByUserId(USER_ID, 5)).thenReturn(List.of(recentSession, oldSession));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TOTAL));

        assertThat(response.averageSessionsText()).isEqualTo("1.4 sesiones/semana");
        assertThat(response.activitySessions()).hasSize(2);
    }

    @Test
    void execute_shouldHandleWeeklyTimeframe_withSingularAndPluralText() {
        ActivitySession session1 = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();
        ActivitySession session2 = ActivitySession.builder()
                .id(2L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(session1));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(session1));
        GetUserActivitiesResponse responseSingular = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.WEEK));
        assertThat(responseSingular.averageSessionsText()).isEqualTo("1 sesión/semana");

        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(session1, session2));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(session1, session2));
        GetUserActivitiesResponse responsePlural = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.WEEK));
        assertThat(responsePlural.averageSessionsText()).isEqualTo("2 sesiones/semana");
    }

    @Test
    void execute_shouldHandleMonthlyTimeframe() {
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        ActivitySession oldSession = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(thirtyDaysAgo)
                .build();
        ActivitySession recentSession = ActivitySession.builder()
                .id(2L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(now)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(oldSession, recentSession));
        when(activitySessionRepository.countByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(1L);
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.of(oldSession));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(recentSession, oldSession));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.MONTH));
        assertThat(response.averageSessionsText()).contains("sesiones/semana");
    }

    @Test
    void execute_shouldHandleTodayTimeframe_withSingularText() {
        ActivitySession session = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(session));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(session));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TODAY));
        assertThat(response.averageSessionsText()).isEqualTo("1 sesión hoy");
    }

    @Test
    void execute_shouldHandleTodayTimeframe_withPluralText() {
        ActivitySession session1 = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();
        ActivitySession session2 = ActivitySession.builder()
                .id(2L)
                .userId(USER_ID)
                .activityType(ActivityType.BREATHING)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(session1, session2));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(session1, session2));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TODAY));
        assertThat(response.averageSessionsText()).isEqualTo("2 sesiones hoy");
    }

    @Test
    void execute_shouldHandleIntegerAveragePerWeekAndEmptyOldestSession() {
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        ActivitySession oldSession = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(sevenDaysAgo)
                .build();
        ActivitySession recentSession = ActivitySession.builder()
                .id(2L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(now)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserId(USER_ID)).thenReturn(List.of(oldSession, recentSession));
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.empty());
        when(activitySessionRepository.findRecentByUserId(USER_ID, 5)).thenReturn(List.of(recentSession, oldSession));

        assertThatThrownBy(() -> useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TOTAL)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing oldest activity session for userId=");
                
        // Now mock oldest session to test integer averagePerWeek
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.of(oldSession));
        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TOTAL));
        assertThat(response.averageSessionsText()).isEqualTo("2 sesiones/semana");
    }

    @Test
    void execute_shouldHandleUnrecognizedActivityType() {
        ActivitySession mockSession = mock(ActivitySession.class);
        ActivityType mockType = mock(ActivityType.class);
        when(mockType.name()).thenReturn("OTHER");
        when(mockSession.getActivityType()).thenReturn(mockType);
        when(mockSession.getId()).thenReturn(1L);
        when(mockSession.getCreatedAt()).thenReturn(Instant.now());

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(mockSession));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(mockSession));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TODAY));
        assertThat(response.activityDistribution()).doesNotContainKey("OTHER");
    }

    @Test
    void execute_shouldHandleZeroSessions() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of());
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of());

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TODAY));
        assertThat(response.averageSessionsText()).isEqualTo("Sin registros");
    }

    @Test
    void execute_shouldHandleAveragePerWeekExactlyOne() {
        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);
        ActivitySession oldSession = ActivitySession.builder()
                .id(1L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARY)
                .createdAt(sevenDaysAgo)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserId(USER_ID)).thenReturn(List.of(oldSession));
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.of(oldSession));
        when(activitySessionRepository.findRecentByUserId(USER_ID, 5)).thenReturn(List.of(oldSession));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TOTAL));
        assertThat(response.averageSessionsText()).isEqualTo("1 sesión/semana");
    }
}

