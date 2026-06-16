package com.huly.backend.domain.useCase.admin.userActivities;

import com.huly.backend.domain.model.ActivitySession;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.ActivitySessionRepository;
import com.huly.backend.domain.repository.UserRepository;
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
                .activityType(ActivityType.RESPIRACION)
                .createdAt(now)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(List.of(session));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5))).thenReturn(List.of(session));

        GetUserActivitiesResponse response = useCase.execute(new GetUserActivitiesRequest(USER_ID, Timeframe.TODAY));

        assertThat(response.todayActivitiesCount()).isEqualTo(1);
        assertThat(response.favoriteActivity()).isEqualTo("RESPIRACION");
        assertThat(response.averageSessionsText()).isEqualTo("1 sesión hoy");
        assertThat(response.activityDistribution()).containsEntry("RESPIRACION", 1);
        assertThat(response.activitySessions()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(10L);
            assertThat(item.activityType()).isEqualTo("RESPIRACION");
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
                .activityType(ActivityType.DIARIO)
                .createdAt(tenDaysAgo)
                .build();
        ActivitySession recentSession = ActivitySession.builder()
                .id(2L)
                .userId(USER_ID)
                .activityType(ActivityType.DIARIO)
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


}
