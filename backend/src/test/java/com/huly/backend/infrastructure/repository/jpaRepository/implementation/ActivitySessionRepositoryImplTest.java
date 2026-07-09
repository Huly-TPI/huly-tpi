package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.activity.ActivitySession;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.repository.entity.ActivitySessionEntity;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivitySessionJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitySessionRepositoryImplTest {

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 10L;
    private static final Instant START = Instant.parse("2026-06-01T00:00:00Z");
    private static final Instant CREATED_AT = Instant.parse("2026-07-01T00:00:00Z");

    @Mock
    private IActivitySessionJpaRepository activitySessionJpaRepository;
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private ActivitySessionRepositoryImpl activitySessionRepository;

    @Test
    @DisplayName("Persiste la sesión y devuelve el modelo de dominio")
    void saveShouldSaveAndReturnDomainModel() {
        givenReferencedUser();
        givenSaved(sessionEntity(SESSION_ID, CREATED_AT));

        ActivitySession result = save(domainSession(CREATED_AT));

        thenSavedSessionMatches(result, SESSION_ID);
    }

    @Test
    @DisplayName("Mapea las sesiones del usuario")
    void findByUserIdShouldReturnMappedList() {
        givenSessionsByUser(sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findByUser();

        thenSessionIdsAre(result, SESSION_ID);
        thenFindByUserDelegated();
    }

    @Test
    @DisplayName("Mapea las sesiones del usuario posteriores a una fecha")
    void findByUserIdAndCreatedAtAfterShouldReturnMappedList() {
        givenSessionsByUserSince(START, sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findByUserSince(START);

        thenSessionIdsAre(result, SESSION_ID);
        thenFindByUserSinceDelegated(START);
    }

    @Test
    @DisplayName("Usa el top 5 cuando el límite recientes es 5")
    void findRecentByUserIdShouldReturnTop5WhenLimitIsFive() {
        givenTop5Sessions(sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findRecent(5);

        thenSessionIdsAre(result, SESSION_ID);
        thenTop5Delegated();
    }

    @Test
    @DisplayName("Devuelve lista vacía de recientes cuando el límite no es positivo")
    void findRecentByUserIdShouldReturnEmptyWhenLimitNotPositive() {
        List<ActivitySession> result = findRecent(0);

        thenSessionsEmpty(result);
    }

    @Test
    @DisplayName("Usa paginación cuando el límite recientes no es 5")
    void findRecentByUserIdShouldUsePaginationWhenLimitIsNotFive() {
        givenPagedSessions(sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findRecent(10);

        thenSessionIdsAre(result, SESSION_ID);
    }

    @Test
    @DisplayName("Usa el top 5 posterior a una fecha cuando el límite recientes es 5")
    void findRecentByUserIdAndCreatedAtAfterShouldReturnTop5WhenLimitIsFive() {
        givenTop5SessionsSince(START, sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findRecentSince(START, 5);

        thenSessionIdsAre(result, SESSION_ID);
        thenTop5SinceDelegated(START);
    }

    @Test
    @DisplayName("Devuelve lista vacía de recientes posteriores a una fecha cuando el límite no es positivo")
    void findRecentByUserIdAndCreatedAtAfterShouldReturnEmptyWhenLimitNotPositive() {
        List<ActivitySession> result = findRecentSince(START, 0);

        thenSessionsEmpty(result);
    }

    @Test
    @DisplayName("Usa paginación posterior a una fecha cuando el límite recientes no es 5")
    void findRecentByUserIdAndCreatedAtAfterShouldUsePaginationWhenLimitIsNotFive() {
        givenPagedSessionsSince(START, sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findRecentSince(START, 10);

        thenSessionIdsAre(result, SESSION_ID);
    }

    @Test
    @DisplayName("Delega el conteo de sesiones posteriores a una fecha")
    void countByUserIdAndCreatedAtAfterShouldReturnCount() {
        givenSessionsCount(START, 15L);

        long result = countSince(START);

        thenCountIs(result, 15L);
        thenCountDelegated(START);
    }

    @Test
    @DisplayName("Devuelve la sesión más antigua cuando existe")
    void findOldestSessionByUserIdShouldReturnOldestWhenPresent() {
        givenOldestSession(sessionEntity(SESSION_ID, CREATED_AT));

        Optional<ActivitySession> result = findOldest();

        thenOldestPresent(result, SESSION_ID);
        thenOldestDelegated();
    }

    @Test
    @DisplayName("Devuelve vacío cuando no hay sesión más antigua")
    void findOldestSessionByUserIdShouldReturnEmptyWhenAbsent() {
        givenOldestSession(null);

        Optional<ActivitySession> result = findOldest();

        thenOldestAbsent(result);
    }

    @Test
    @DisplayName("Mapea todas las sesiones")
    void findAllShouldReturnMappedList() {
        givenAllSessions(sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findAll();

        thenSessionIdsAre(result, SESSION_ID);
    }

    @Test
    @DisplayName("Devuelve todas las sesiones cuando la fecha de inicio es nula")
    void findAllAfterShouldReturnAllWhenStartIsNull() {
        givenAllSessions(sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findAllAfter(null);

        thenSessionIdsAre(result, SESSION_ID);
    }

    @Test
    @DisplayName("Devuelve las sesiones posteriores a la fecha cuando la fecha de inicio no es nula")
    void findAllAfterShouldReturnSessionsAfterStartWhenStartIsNotNull() {
        givenSessionsCreatedAfter(START, sessionEntity(SESSION_ID, CREATED_AT));

        List<ActivitySession> result = findAllAfter(START);

        thenSessionIdsAre(result, SESSION_ID);
    }

    // --- arrange ---
    private void givenReferencedUser() {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(userEntity());
    }

    private void givenSaved(ActivitySessionEntity entity) {
        when(activitySessionJpaRepository.save(any(ActivitySessionEntity.class))).thenReturn(entity);
    }

    private void givenSessionsByUser(ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findByUserId(USER_ID)).thenReturn(List.of(entities));
    }

    private void givenSessionsByUserSince(Instant start, ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findByUserIdAndCreatedAtAfter(USER_ID, start)).thenReturn(List.of(entities));
    }

    private void givenTop5Sessions(ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findTop5ByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(entities));
    }

    private void givenTop5SessionsSince(Instant start, ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(USER_ID, start))
                .thenReturn(List.of(entities));
    }

    private void givenPagedSessions(ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findByUserId(eq(USER_ID), any(Pageable.class))).thenReturn(List.of(entities));
    }

    private void givenPagedSessionsSince(Instant start, ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findByUserIdAndCreatedAtAfter(eq(USER_ID), eq(start), any(Pageable.class)))
                .thenReturn(List.of(entities));
    }

    private void givenSessionsCount(Instant start, long count) {
        when(activitySessionJpaRepository.countByUserIdAndCreatedAtAfter(USER_ID, start)).thenReturn(count);
    }

    private void givenOldestSession(ActivitySessionEntity entity) {
        when(activitySessionJpaRepository.findFirstByUserIdOrderByCreatedAtAsc(USER_ID))
                .thenReturn(Optional.ofNullable(entity));
    }

    private void givenAllSessions(ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findAll()).thenReturn(List.of(entities));
    }

    private void givenSessionsCreatedAfter(Instant start, ActivitySessionEntity... entities) {
        when(activitySessionJpaRepository.findByCreatedAtAfter(start)).thenReturn(List.of(entities));
    }

    private AppUserEntity userEntity() {
        AppUserEntity entity = new AppUserEntity();
        entity.setId(USER_ID);
        return entity;
    }

    private ActivitySession domainSession(Instant createdAt) {
        return ActivitySession.builder()
                .userId(USER_ID)
                .activityType(ActivityType.BREATHING)
                .createdAt(createdAt)
                .build();
    }

    private ActivitySessionEntity sessionEntity(Long id, Instant createdAt) {
        return ActivitySessionEntity.builder()
                .id(id)
                .user(userEntity())
                .activityType(ActivityType.BREATHING)
                .createdAt(createdAt)
                .build();
    }

    // --- act ---
    private ActivitySession save(ActivitySession session) {
        return activitySessionRepository.save(session);
    }

    private List<ActivitySession> findByUser() {
        return activitySessionRepository.findByUserId(USER_ID);
    }

    private List<ActivitySession> findByUserSince(Instant start) {
        return activitySessionRepository.findByUserIdAndCreatedAtAfter(USER_ID, start);
    }

    private List<ActivitySession> findRecent(int limit) {
        return activitySessionRepository.findRecentByUserId(USER_ID, limit);
    }

    private List<ActivitySession> findRecentSince(Instant start, int limit) {
        return activitySessionRepository.findRecentByUserIdAndCreatedAtAfter(USER_ID, start, limit);
    }

    private long countSince(Instant start) {
        return activitySessionRepository.countByUserIdAndCreatedAtAfter(USER_ID, start);
    }

    private Optional<ActivitySession> findOldest() {
        return activitySessionRepository.findOldestSessionByUserId(USER_ID);
    }

    private List<ActivitySession> findAll() {
        return activitySessionRepository.findAll();
    }

    private List<ActivitySession> findAllAfter(Instant start) {
        return activitySessionRepository.findAllAfter(start);
    }

    // --- assert ---
    private void thenSavedSessionMatches(ActivitySession result, Long id) {
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getActivityType()).isEqualTo(ActivityType.BREATHING);
        verify(appUserRepository).getReferenceById(USER_ID);
        verify(activitySessionJpaRepository).save(any(ActivitySessionEntity.class));
    }

    private void thenSessionIdsAre(List<ActivitySession> result, Long... ids) {
        assertThat(result).extracting(ActivitySession::getId).containsExactly(ids);
    }

    private void thenSessionsEmpty(List<ActivitySession> result) {
        assertThat(result).isEmpty();
    }

    private void thenCountIs(long result, long expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenOldestPresent(Optional<ActivitySession> result, Long id) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }

    private void thenOldestAbsent(Optional<ActivitySession> result) {
        assertThat(result).isEmpty();
    }

    private void thenFindByUserDelegated() {
        verify(activitySessionJpaRepository).findByUserId(USER_ID);
    }

    private void thenFindByUserSinceDelegated(Instant start) {
        verify(activitySessionJpaRepository).findByUserIdAndCreatedAtAfter(USER_ID, start);
    }

    private void thenTop5Delegated() {
        verify(activitySessionJpaRepository).findTop5ByUserIdOrderByCreatedAtDesc(USER_ID);
    }

    private void thenTop5SinceDelegated(Instant start) {
        verify(activitySessionJpaRepository).findTop5ByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(USER_ID, start);
    }

    private void thenCountDelegated(Instant start) {
        verify(activitySessionJpaRepository).countByUserIdAndCreatedAtAfter(USER_ID, start);
    }

    private void thenOldestDelegated() {
        verify(activitySessionJpaRepository).findFirstByUserIdOrderByCreatedAtAsc(USER_ID);
    }
}
