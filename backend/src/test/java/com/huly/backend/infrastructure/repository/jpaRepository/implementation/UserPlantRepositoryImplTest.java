package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.GoalStatus;
import com.huly.backend.domain.model.enums.PlantStatus;
import com.huly.backend.domain.model.user.UserPlant;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserPlantEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserPlantJpaRepository;
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
class UserPlantRepositoryImplTest {

    private static final Long USER_ID = 7L;
    private static final Instant STARTED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private IUserPlantJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserPlantRepositoryImpl repository;

    @Test
    @DisplayName("Mapea el dominio a entidad antes de persistir con save")
    void saveShouldMapDomainToEntityBeforePersisting() {
        givenReferencedUser();
        givenSaved(persistedPlant(1L));

        save(domainPlant());

        thenPersistedEntityMatches();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio con save")
    void saveShouldMapPersistedEntityToDomain() {
        givenReferencedUser();
        givenSaved(persistedPlant(1L));

        UserPlant result = save(domainPlant());

        thenPlantMatches(result, 1L);
    }

    @Test
    @DisplayName("Persiste con saveAndFlush y mapea el resultado a dominio")
    void saveAndFlushShouldPersistAndMapToDomain() {
        givenReferencedUser();
        givenSavedAndFlushed(persistedPlant(2L));

        UserPlant result = saveAndFlush(domainPlant());

        thenPlantMatches(result, 2L);
        thenFlushed();
    }

    @Test
    @DisplayName("Devuelve la última planta por usuario y estado cuando existe")
    void findLatestByUserIdAndStatusShouldReturnMappedPlantWhenPresent() {
        givenLatestByStatus(PlantStatus.GROWING, persistedPlant(3L));

        Optional<UserPlant> result = findLatestByStatus(PlantStatus.GROWING);

        thenPlantPresent(result, 3L);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por usuario y estado cuando no hay planta")
    void findLatestByUserIdAndStatusShouldReturnEmptyWhenAbsent() {
        givenLatestByStatus(PlantStatus.COMPLETED, null);

        Optional<UserPlant> result = findLatestByStatus(PlantStatus.COMPLETED);

        thenAbsent(result);
    }

    @Test
    @DisplayName("Devuelve la última planta por usuario cuando existe")
    void findLatestByUserIdShouldReturnMappedPlantWhenPresent() {
        givenLatest(persistedPlant(4L));

        Optional<UserPlant> result = findLatest();

        thenPlantPresent(result, 4L);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar la última planta por usuario cuando no hay")
    void findLatestByUserIdShouldReturnEmptyWhenAbsent() {
        givenLatest(null);

        Optional<UserPlant> result = findLatest();

        thenAbsent(result);
    }

    @Test
    @DisplayName("Mapea todas las plantas del usuario ordenadas por número")
    void findAllByUserIdOrderByPlantNumberShouldMapEntities() {
        givenAllPlants(persistedPlant(1L), persistedPlant(2L));

        List<UserPlant> result = findAll();

        thenPlantIdsAre(result, 1L, 2L);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando el usuario no tiene plantas")
    void findAllByUserIdOrderByPlantNumberShouldReturnEmptyWhenNone() {
        givenAllPlants();

        List<UserPlant> result = findAll();

        thenEmpty(result);
    }

    @Test
    @DisplayName("Delega el conteo de objetivos completados por planta")
    void countCompletedGoalsByPlantIdShouldDelegateToJpa() {
        givenCompletedGoalsCount(9L, 3L);

        long result = countCompletedGoals(9L);

        thenCountIs(result, 3L);
    }

    // --- arrange ---
    private void givenReferencedUser() {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(appUserEntity(USER_ID));
    }

    private void givenSaved(UserPlantEntity entity) {
        when(jpaRepository.save(any())).thenReturn(entity);
    }

    private void givenSavedAndFlushed(UserPlantEntity entity) {
        when(jpaRepository.saveAndFlush(any())).thenReturn(entity);
    }

    private void givenLatestByStatus(PlantStatus status, UserPlantEntity entity) {
        when(jpaRepository.findTopByAppUser_IdAndStatusOrderByPlantNumberDescStartedAtDescIdDesc(USER_ID, status))
                .thenReturn(Optional.ofNullable(entity));
    }

    private void givenLatest(UserPlantEntity entity) {
        when(jpaRepository.findTopByAppUser_IdOrderByPlantNumberDescStartedAtDescIdDesc(USER_ID))
                .thenReturn(Optional.ofNullable(entity));
    }

    private void givenAllPlants(UserPlantEntity... entities) {
        when(jpaRepository.findByAppUser_IdOrderByPlantNumberAsc(USER_ID)).thenReturn(List.of(entities));
    }

    private void givenCompletedGoalsCount(Long plantId, long count) {
        when(jpaRepository.countByUserPlantIdAndStatus(plantId, GoalStatus.COMPLETED)).thenReturn(count);
    }

    private UserPlant domainPlant() {
        return UserPlant.builder()
                .userId(USER_ID).plantNumber(2).requiredGoals(5)
                .status(PlantStatus.GROWING).startedAt(STARTED_AT).build();
    }

    private UserPlantEntity persistedPlant(Long id) {
        return UserPlantEntity.builder()
                .id(id).appUser(appUserEntity(USER_ID)).plantNumber(2).requiredGoals(5)
                .status(PlantStatus.GROWING).startedAt(STARTED_AT).build();
    }

    private AppUserEntity appUserEntity(Long id) {
        AppUserEntity entity = new AppUserEntity();
        entity.setId(id);
        return entity;
    }

    // --- act ---
    private UserPlant save(UserPlant domain) {
        return repository.save(domain);
    }

    private UserPlant saveAndFlush(UserPlant domain) {
        return repository.saveAndFlush(domain);
    }

    private Optional<UserPlant> findLatestByStatus(PlantStatus status) {
        return repository.findLatestByUserIdAndStatus(USER_ID, status);
    }

    private Optional<UserPlant> findLatest() {
        return repository.findLatestByUserId(USER_ID);
    }

    private List<UserPlant> findAll() {
        return repository.findAllByUserIdOrderByPlantNumber(USER_ID);
    }

    private long countCompletedGoals(Long plantId) {
        return repository.countCompletedGoalsByPlantId(plantId);
    }

    // --- assert ---
    private void thenPersistedEntityMatches() {
        ArgumentCaptor<UserPlantEntity> captor = ArgumentCaptor.forClass(UserPlantEntity.class);
        verify(jpaRepository).save(captor.capture());
        UserPlantEntity persisted = captor.getValue();
        assertThat(persisted.getAppUser().getId()).isEqualTo(USER_ID);
        assertThat(persisted.getPlantNumber()).isEqualTo(2);
        assertThat(persisted.getRequiredGoals()).isEqualTo(5);
        assertThat(persisted.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(persisted.getStartedAt()).isEqualTo(STARTED_AT);
    }

    private void thenPlantMatches(UserPlant result, Long expectedId) {
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getPlantNumber()).isEqualTo(2);
        assertThat(result.getRequiredGoals()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(PlantStatus.GROWING);
        assertThat(result.getStartedAt()).isEqualTo(STARTED_AT);
    }

    private void thenPlantPresent(Optional<UserPlant> result, Long expectedId) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expectedId);
        assertThat(result.get().getUserId()).isEqualTo(USER_ID);
    }

    private void thenPlantIdsAre(List<UserPlant> result, Long... ids) {
        assertThat(result).extracting(UserPlant::getId).containsExactly(ids);
    }

    private void thenFlushed() {
        verify(jpaRepository).saveAndFlush(any());
    }

    private void thenAbsent(Optional<UserPlant> result) {
        assertThat(result).isEmpty();
    }

    private void thenEmpty(List<UserPlant> result) {
        assertThat(result).isEmpty();
    }

    private void thenCountIs(long result, long expected) {
        assertThat(result).isEqualTo(expected);
    }
}
