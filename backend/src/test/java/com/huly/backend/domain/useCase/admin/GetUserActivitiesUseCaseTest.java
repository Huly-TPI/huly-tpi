package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.Timeframe;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import com.huly.backend.domain.repository.activity.ActivitySessionRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesRequest;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesResponse;
import com.huly.backend.domain.useCase.admin.userActivities.GetUserActivitiesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserActivitiesUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Instant NOW = Instant.now();

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivitySessionRepository activitySessionRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private GetUserActivitiesUseCase useCase;

    @Test
    @DisplayName("Lanza excepción cuando el usuario no existe")
    void executeShouldThrowWhenUserNotFound() {
        // --- arrange ---
        givenUserNotFound();

        // --- assert ---
        thenActivitiesThrowsUserNotFound(Timeframe.TOTAL);
    }

    @Test
    @DisplayName("Filtra en base de datos y arma la respuesta para el rango de hoy")
    void executeShouldBuildResponseFromDatabaseFilteringForToday() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(10L, ActivityType.BREATHING, NOW));
        givenRecentSessionsAfterStart(session(10L, ActivityType.BREATHING, NOW));
        givenActivityCatalog(
                activity(ActivityType.BREATHING, "Respiración guiada"),
                activity(ActivityType.DIARY, "Diario emocional"),
                activity(ActivityType.BUBBLE, "Burbujas relajantes"));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenTodayBreathingResponse(response);
    }

    @Test
    @DisplayName("Calcula el promedio semanal para el rango total")
    void executeShouldCalculateWeeklyAverageForTotalTimeframe() {
        // --- arrange ---
        givenUserExists();
        givenAllSessions(session(1L, ActivityType.DIARY, daysAgo(10)), session(2L, ActivityType.DIARY, NOW));
        givenCountAfter(1L);
        givenOldestSession(session(1L, ActivityType.DIARY, daysAgo(10)));
        givenRecentSessions(session(2L, ActivityType.DIARY, NOW), session(1L, ActivityType.DIARY, daysAgo(10)));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TOTAL);

        // --- assert ---
        thenAverageTextAndSize(response, "1.4 sesiones/semana", 2);
    }

    @Test
    @DisplayName("Usa texto singular para una única sesión semanal")
    void executeShouldRenderSingularWeeklyText() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW));
        givenRecentSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.WEEK);

        // --- assert ---
        thenAverageText(response, "1 sesión/semana");
    }

    @Test
    @DisplayName("Usa texto plural para varias sesiones semanales")
    void executeShouldRenderPluralWeeklyText() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW), session(2L, ActivityType.DIARY, NOW));
        givenRecentSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW), session(2L, ActivityType.DIARY, NOW));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.WEEK);

        // --- assert ---
        thenAverageText(response, "2 sesiones/semana");
    }

    @Test
    @DisplayName("Resuelve el rango mensual en promedio por semana")
    void executeShouldHandleMonthlyTimeframe() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(1L, ActivityType.DIARY, daysAgo(30)), session(2L, ActivityType.DIARY, NOW));
        givenCountAfter(1L);
        givenOldestSession(session(1L, ActivityType.DIARY, daysAgo(30)));
        givenRecentSessionsAfterStart(session(2L, ActivityType.DIARY, NOW), session(1L, ActivityType.DIARY, daysAgo(30)));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.MONTH);

        // --- assert ---
        thenAverageTextContains(response, "sesiones/semana");
    }

    @Test
    @DisplayName("Usa texto singular para una única sesión de hoy")
    void executeShouldRenderSingularTodayText() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW));
        givenRecentSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenAverageText(response, "1 sesión hoy");
    }

    @Test
    @DisplayName("Usa texto plural para varias sesiones de hoy")
    void executeShouldRenderPluralTodayText() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW), session(2L, ActivityType.BREATHING, NOW));
        givenRecentSessionsAfterStart(session(1L, ActivityType.BREATHING, NOW), session(2L, ActivityType.BREATHING, NOW));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenAverageText(response, "2 sesiones hoy");
    }

    @Test
    @DisplayName("Lanza excepción cuando falta la sesión más antigua en el rango total")
    void executeShouldThrowWhenOldestSessionMissingForTotalTimeframe() {
        // --- arrange ---
        givenUserExists();
        givenAllSessions(session(1L, ActivityType.DIARY, daysAgo(7)), session(2L, ActivityType.DIARY, NOW));
        givenNoOldestSession();

        // --- assert ---
        thenActivitiesThrowsMissingOldest(Timeframe.TOTAL);
    }

    @Test
    @DisplayName("Renderiza promedio entero por semana en el rango total")
    void executeShouldRenderIntegerAveragePerWeekForTotalTimeframe() {
        // --- arrange ---
        givenUserExists();
        givenAllSessions(session(1L, ActivityType.DIARY, daysAgo(7)), session(2L, ActivityType.DIARY, NOW));
        givenOldestSession(session(1L, ActivityType.DIARY, daysAgo(7)));
        givenRecentSessions(session(2L, ActivityType.DIARY, NOW), session(1L, ActivityType.DIARY, daysAgo(7)));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TOTAL);

        // --- assert ---
        thenAverageText(response, "2 sesiones/semana");
    }

    @Test
    @DisplayName("Renderiza promedio de exactamente una sesión por semana")
    void executeShouldRenderAveragePerWeekExactlyOne() {
        // --- arrange ---
        givenUserExists();
        givenAllSessions(session(1L, ActivityType.DIARY, daysAgo(7)));
        givenOldestSession(session(1L, ActivityType.DIARY, daysAgo(7)));
        givenRecentSessions(session(1L, ActivityType.DIARY, daysAgo(7)));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TOTAL);

        // --- assert ---
        thenAverageText(response, "1 sesión/semana");
    }

    @Test
    @DisplayName("Ignora en la distribución los tipos de actividad desconocidos")
    void executeShouldIgnoreUnrecognizedActivityType() {
        // --- arrange ---
        givenUserExists();
        givenUnrecognizedTypeSessionForToday();

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenDistributionExcludesUnrecognized(response);
    }

    @Test
    @DisplayName("Devuelve 'Sin registros' cuando no hay sesiones")
    void executeShouldHandleZeroSessions() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart();
        givenRecentSessionsAfterStart();

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenAverageText(response, "Sin registros");
    }

    @Test
    @DisplayName("Ignora el fallo del catálogo de actividades y usa el tipo como nombre")
    void executeShouldIgnoreActivityCatalogFailure() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(10L, ActivityType.BREATHING, NOW));
        givenRecentSessionsAfterStart(session(10L, ActivityType.BREATHING, NOW));
        givenActivityCatalogFails();

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenActivityNamesEmptyWithTypeFallback(response);
    }

    @Test
    @DisplayName("Omite las entradas del catálogo con tipo o título nulos")
    void executeShouldSkipCatalogEntriesWithNullFields() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(10L, ActivityType.BREATHING, NOW));
        givenRecentSessionsAfterStart(session(10L, ActivityType.BREATHING, NOW));
        givenActivityCatalog(
                activity(ActivityType.BREATHING, "Respiración guiada"),
                activity(ActivityType.DIARY, null),
                activity(null, "Huérfano"));

        // --- act ---
        GetUserActivitiesResponse response = activitiesFor(Timeframe.TODAY);

        // --- assert ---
        thenOnlyBreathingCatalogEntry(response);
    }

    @Test
    @DisplayName("Lanza excepción cuando una sesión filtrada no tiene tipo de actividad")
    void executeShouldThrowWhenSessionActivityTypeIsNull() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart(session(1L, null, NOW));

        // --- assert ---
        thenActivitiesThrowsNullActivityType(Timeframe.TODAY);
    }

    @Test
    @DisplayName("Lanza excepción cuando una sesión reciente no tiene tipo de actividad")
    void executeShouldThrowWhenRecentSessionActivityTypeIsNull() {
        // --- arrange ---
        givenUserExists();
        givenSessionsAfterStart();
        givenRecentSessionsAfterStart(session(1L, null, NOW));

        // --- assert ---
        thenActivitiesThrowsNullActivityType(Timeframe.TODAY);
    }

    // --- arrange ---

    private void givenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
    }

    private void givenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenSessionsAfterStart(ActivitySession... sessions) {
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class)))
                .thenReturn(List.of(sessions));
    }

    private void givenRecentSessionsAfterStart(ActivitySession... sessions) {
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5)))
                .thenReturn(List.of(sessions));
    }

    private void givenAllSessions(ActivitySession... sessions) {
        when(activitySessionRepository.findByUserId(USER_ID)).thenReturn(List.of(sessions));
    }

    private void givenRecentSessions(ActivitySession... sessions) {
        when(activitySessionRepository.findRecentByUserId(USER_ID, 5)).thenReturn(List.of(sessions));
    }

    private void givenCountAfter(long count) {
        when(activitySessionRepository.countByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class))).thenReturn(count);
    }

    private void givenOldestSession(ActivitySession session) {
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.of(session));
    }

    private void givenNoOldestSession() {
        when(activitySessionRepository.findOldestSessionByUserId(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenActivityCatalog(Activity... activities) {
        when(activityRepository.findAll()).thenReturn(List.of(activities));
    }

    private void givenActivityCatalogFails() {
        when(activityRepository.findAll()).thenThrow(new RuntimeException("catalog unavailable"));
    }

    private void givenUnrecognizedTypeSessionForToday() {
        ActivitySession mockSession = mock(ActivitySession.class);
        ActivityType mockType = mock(ActivityType.class);
        when(mockType.name()).thenReturn("OTHER");
        when(mockSession.getActivityType()).thenReturn(mockType);
        when(mockSession.getId()).thenReturn(1L);
        when(mockSession.getCreatedAt()).thenReturn(NOW);
        when(activitySessionRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class)))
                .thenReturn(List.of(mockSession));
        when(activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(eq(USER_ID), any(Instant.class), eq(5)))
                .thenReturn(List.of(mockSession));
    }

    private ActivitySession session(Long id, ActivityType type, Instant createdAt) {
        return ActivitySession.builder()
                .id(id)
                .userId(USER_ID)
                .activityType(type)
                .createdAt(createdAt)
                .build();
    }

    private Activity activity(ActivityType type, String title) {
        return Activity.builder().type(type).title(title).build();
    }

    private Instant daysAgo(int days) {
        return NOW.minus(days, ChronoUnit.DAYS);
    }

    // --- act ---

    private GetUserActivitiesResponse activitiesFor(Timeframe timeframe) {
        return useCase.execute(new GetUserActivitiesRequest(USER_ID, timeframe));
    }

    // --- assert ---

    private void thenTodayBreathingResponse(GetUserActivitiesResponse response) {
        assertThat(response.todayActivitiesCount()).isEqualTo(1);
        assertThat(response.favoriteActivity()).isEqualTo("BREATHING");
        assertThat(response.averageSessionsText()).isEqualTo("1 sesión hoy");
        assertThat(response.activityDistribution()).containsEntry("BREATHING", 1);
        assertThat(response.activityNames()).containsEntry("BREATHING", "Respiración guiada");
        assertThat(response.activitySessions()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(10L);
            assertThat(item.activityType()).isEqualTo("BREATHING");
            assertThat(item.activityName()).isEqualTo("Respiración guiada");
            assertThat(item.createdAt()).isEqualTo(NOW);
        });
    }

    private void thenAverageTextAndSize(GetUserActivitiesResponse response, String expectedText, int expectedSize) {
        assertThat(response.averageSessionsText()).isEqualTo(expectedText);
        assertThat(response.activitySessions()).hasSize(expectedSize);
    }

    private void thenAverageText(GetUserActivitiesResponse response, String expectedText) {
        assertThat(response.averageSessionsText()).isEqualTo(expectedText);
    }

    private void thenAverageTextContains(GetUserActivitiesResponse response, String fragment) {
        assertThat(response.averageSessionsText()).contains(fragment);
    }

    private void thenDistributionExcludesUnrecognized(GetUserActivitiesResponse response) {
        assertThat(response.activityDistribution()).doesNotContainKey("OTHER");
    }

    private void thenActivityNamesEmptyWithTypeFallback(GetUserActivitiesResponse response) {
        assertThat(response.activityNames()).isEmpty();
        assertThat(response.activitySessions()).singleElement().satisfies(item ->
                assertThat(item.activityName()).isEqualTo("BREATHING"));
    }

    private void thenOnlyBreathingCatalogEntry(GetUserActivitiesResponse response) {
        assertThat(response.activityNames()).hasSize(1);
        assertThat(response.activityNames()).containsEntry("BREATHING", "Respiración guiada");
        assertThat(response.activityNames()).doesNotContainKey("DIARY");
    }

    private void thenActivitiesThrowsUserNotFound(Timeframe timeframe) {
        assertThatThrownBy(() -> activitiesFor(timeframe))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    private void thenActivitiesThrowsMissingOldest(Timeframe timeframe) {
        assertThatThrownBy(() -> activitiesFor(timeframe))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing oldest activity session for userId=");
    }

    private void thenActivitiesThrowsNullActivityType(Timeframe timeframe) {
        assertThatThrownBy(() -> activitiesFor(timeframe))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ActivitySession activityType is required");
    }
}
