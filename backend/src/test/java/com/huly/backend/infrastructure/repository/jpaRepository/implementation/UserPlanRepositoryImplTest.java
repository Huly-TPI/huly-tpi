package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.UserPlan;
import com.huly.backend.infrastructure.repository.entity.UserPlanEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlanJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPlanRepositoryImplTest {

    private static final Long PLAN_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long MISSING_USER_ID = 99L;
    private static final Long PRODUCT_ID = 7L;
    private static final String PLAN_CODE = "PREMIUM";
    private static final Long SAVE_PRODUCT_ID = 8L;
    private static final String SAVE_PLAN_CODE = "PRO";
    private static final Instant GRANTED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-15T00:00:00Z");
    private static final Instant THRESHOLD = Instant.parse("2026-01-22T00:00:00Z");

    @Mock
    private IUserPlanJpaRepository jpaRepository;

    @InjectMocks
    private UserPlanRepositoryImpl repository;

    @Test
    @DisplayName("Devuelve el plan mapeado por usuario cuando existe")
    void findByUserShouldReturnMappedPlanWhenFound() {
        givenPlanByUser(USER_ID, planEntity(PLAN_ID, USER_ID, PRODUCT_ID, PLAN_CODE));

        Optional<UserPlan> result = findByUser(USER_ID);

        thenPlanMatches(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar el plan por usuario cuando no existe")
    void findByUserShouldReturnEmptyWhenNotFound() {
        givenPlanMissing(MISSING_USER_ID);

        Optional<UserPlan> result = findByUser(MISSING_USER_ID);

        thenAbsent(result);
    }

    @Test
    @DisplayName("Mapea el dominio a entidad antes de persistir con save")
    void saveShouldMapDomainToEntity() {
        givenSaved(planEntity(PLAN_ID, USER_ID, SAVE_PRODUCT_ID, SAVE_PLAN_CODE));

        save(saveDomain());

        thenPersistedPlanMatches();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio con save")
    void saveShouldMapPersistedEntityToDomain() {
        givenSaved(planEntity(PLAN_ID, USER_ID, SAVE_PRODUCT_ID, SAVE_PLAN_CODE));

        UserPlan result = save(saveDomain());

        thenSavedPlanMatches(result);
    }

    @Test
    @DisplayName("Mapea los planes que requieren aviso de vencimiento")
    void findPlansNeedingExpiryReminderShouldMapEntities() {
        givenPlansNeedingReminder(
                planEntity(1L, USER_ID, PRODUCT_ID, PLAN_CODE),
                planEntity(2L, USER_ID, PRODUCT_ID, PLAN_CODE));

        List<UserPlan> result = findPlansNeedingReminder();

        thenPlanIdsAre(result, 1L, 2L);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando ningún plan requiere aviso de vencimiento")
    void findPlansNeedingExpiryReminderShouldReturnEmptyWhenNone() {
        givenPlansNeedingReminder();

        List<UserPlan> result = findPlansNeedingReminder();

        thenPlansEmpty(result);
    }

    @Test
    @DisplayName("Delega el marcado del aviso de vencimiento al repositorio JPA")
    void markExpiryReminderSentShouldDelegateToJpa() {
        markExpiryReminderSent(PLAN_ID, EXPIRES_AT);

        thenReminderMarked(PLAN_ID, EXPIRES_AT);
    }

    // --- arrange ---
    private void givenPlanByUser(Long userId, UserPlanEntity entity) {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
    }

    private void givenPlanMissing(Long userId) {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.empty());
    }

    private void givenSaved(UserPlanEntity entity) {
        when(jpaRepository.save(any(UserPlanEntity.class))).thenReturn(entity);
    }

    private void givenPlansNeedingReminder(UserPlanEntity... entities) {
        when(jpaRepository.findPlansNeedingExpiryReminder(NOW, THRESHOLD)).thenReturn(List.of(entities));
    }

    private UserPlan saveDomain() {
        return UserPlan.builder()
                .id(PLAN_ID).userId(USER_ID).productId(SAVE_PRODUCT_ID).planCode(SAVE_PLAN_CODE)
                .grantedAt(GRANTED_AT).expiresAt(EXPIRES_AT)
                .build();
    }

    private UserPlanEntity planEntity(Long id, Long userId, Long productId, String planCode) {
        return UserPlanEntity.builder()
                .id(id).userId(userId).productId(productId).planCode(planCode)
                .grantedAt(GRANTED_AT).expiresAt(EXPIRES_AT)
                .build();
    }

    // --- act ---
    private Optional<UserPlan> findByUser(Long userId) {
        return repository.findByUser(userId);
    }

    private UserPlan save(UserPlan domain) {
        return repository.save(domain);
    }

    private List<UserPlan> findPlansNeedingReminder() {
        return repository.findPlansNeedingExpiryReminder(NOW, THRESHOLD);
    }

    private void markExpiryReminderSent(Long id, Instant expiresAt) {
        repository.markExpiryReminderSent(id, expiresAt);
    }

    // --- assert ---
    private void thenPlanMatches(Optional<UserPlan> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(PLAN_ID);
        assertThat(result.get().getUserId()).isEqualTo(USER_ID);
        assertThat(result.get().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(result.get().getPlanCode()).isEqualTo(PLAN_CODE);
        assertThat(result.get().getGrantedAt()).isEqualTo(GRANTED_AT);
        assertThat(result.get().getExpiresAt()).isEqualTo(EXPIRES_AT);
    }

    private void thenAbsent(Optional<UserPlan> result) {
        assertThat(result).isEmpty();
    }

    private void thenPersistedPlanMatches() {
        ArgumentCaptor<UserPlanEntity> captor = ArgumentCaptor.forClass(UserPlanEntity.class);
        verify(jpaRepository).save(captor.capture());
        UserPlanEntity persisted = captor.getValue();
        assertThat(persisted.getId()).isEqualTo(PLAN_ID);
        assertThat(persisted.getUserId()).isEqualTo(USER_ID);
        assertThat(persisted.getProductId()).isEqualTo(SAVE_PRODUCT_ID);
        assertThat(persisted.getPlanCode()).isEqualTo(SAVE_PLAN_CODE);
        assertThat(persisted.getGrantedAt()).isEqualTo(GRANTED_AT);
        assertThat(persisted.getExpiresAt()).isEqualTo(EXPIRES_AT);
    }

    private void thenSavedPlanMatches(UserPlan result) {
        assertThat(result.getId()).isEqualTo(PLAN_ID);
        assertThat(result.getProductId()).isEqualTo(SAVE_PRODUCT_ID);
        assertThat(result.getPlanCode()).isEqualTo(SAVE_PLAN_CODE);
    }

    private void thenPlanIdsAre(List<UserPlan> result, Long... ids) {
        assertThat(result).extracting(UserPlan::getId).containsExactly(ids);
    }

    private void thenPlansEmpty(List<UserPlan> result) {
        assertThat(result).isEmpty();
    }

    private void thenReminderMarked(Long id, Instant expiresAt) {
        verify(jpaRepository).markExpiryReminderSent(id, expiresAt);
    }
}
