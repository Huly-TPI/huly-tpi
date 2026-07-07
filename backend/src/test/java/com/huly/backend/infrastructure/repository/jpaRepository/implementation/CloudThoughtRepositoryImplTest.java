package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.CloudThought;
import com.huly.backend.domain.model.enums.CloudStatus;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.CloudThoughtEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.ICloudThoughtJpaRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudThoughtRepositoryImplTest {

    private static final Long USER_ID = 3L;
    private static final Long THOUGHT_ID = 55L;
    private static final Instant CREATED_AT = Instant.parse("2026-02-02T08:00:00Z");

    @Mock
    private ICloudThoughtJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private CloudThoughtRepositoryImpl repository;

    @Test
    @DisplayName("Guarda un pensamiento nuevo como ACTIVE y sin trabajar")
    void saveShouldPersistActiveNotWorkedThought() {
        givenReferencedUser();
        givenSaved(persistedThought(THOUGHT_ID, CloudStatus.ACTIVE, false));

        save("hola");

        thenPersistedThoughtIsActiveAndNotWorked();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio al guardar")
    void saveShouldMapPersistedEntityToDomain() {
        givenReferencedUser();
        givenSaved(persistedThought(THOUGHT_ID, CloudStatus.ACTIVE, false));

        CloudThought result = save("hola");

        thenThoughtMatches(result, THOUGHT_ID, CloudStatus.ACTIVE);
    }

    @Test
    @DisplayName("Mapea los pensamientos activos del usuario")
    void findAllByUserIdShouldMapEntities() {
        givenActiveThoughts(
                persistedThought(1L, CloudStatus.ACTIVE, false),
                persistedThought(2L, CloudStatus.ACTIVE, true));

        List<CloudThought> result = findAll();

        thenThoughtIdsAre(result, 1L, 2L);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando el usuario no tiene pensamientos")
    void findAllByUserIdShouldReturnEmptyWhenNone() {
        givenActiveThoughts();

        List<CloudThought> result = findAll();

        thenEmpty(result);
    }

    @Test
    @DisplayName("Devuelve el pensamiento por id y usuario cuando existe")
    void findByIdAndUserIdShouldReturnMappedThoughtWhenPresent() {
        givenThoughtByIdAndUser(persistedThought(THOUGHT_ID, CloudStatus.ACTIVE, false));

        Optional<CloudThought> result = findByIdAndUser();

        thenThoughtPresent(result, THOUGHT_ID);
    }

    @Test
    @DisplayName("Devuelve vacío cuando no existe el pensamiento por id y usuario")
    void findByIdAndUserIdShouldReturnEmptyWhenAbsent() {
        givenThoughtByIdAndUser(null);

        Optional<CloudThought> result = findByIdAndUser();

        thenAbsent(result);
    }

    @Test
    @DisplayName("Actualiza el estado del pensamiento cuando existe")
    void updateStatusShouldPersistNewStatusWhenFound() {
        CloudThoughtEntity entity = persistedThought(THOUGHT_ID, CloudStatus.ACTIVE, false);
        givenThoughtById(entity);
        givenSaved(entity);

        CloudThought result = updateStatus(CloudStatus.COMPLETED);

        thenStatusUpdated(entity, result, CloudStatus.COMPLETED);
    }

    @Test
    @DisplayName("Falla al actualizar el estado cuando el pensamiento no existe")
    void updateStatusShouldThrowWhenNotFound() {
        givenThoughtById(null);

        thenUpdateStatusThrowsNotFound();
    }

    @Test
    @DisplayName("Marca el pensamiento como trabajado cuando existe")
    void markWorkedOnShouldPersistFlagWhenFound() {
        CloudThoughtEntity entity = persistedThought(THOUGHT_ID, CloudStatus.ACTIVE, false);
        givenThoughtById(entity);

        markWorkedOn();

        thenMarkedWorkedOn(entity);
    }

    @Test
    @DisplayName("Falla al marcar como trabajado cuando el pensamiento no existe")
    void markWorkedOnShouldThrowWhenNotFound() {
        givenThoughtById(null);

        thenMarkWorkedOnThrowsNotFound();
    }

    // --- arrange ---
    private void givenReferencedUser() {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(appUserEntity(USER_ID));
    }

    private void givenSaved(CloudThoughtEntity entity) {
        when(jpaRepository.save(any())).thenReturn(entity);
    }

    private void givenActiveThoughts(CloudThoughtEntity... entities) {
        when(jpaRepository.findAllByUser_IdAndStatusOrderByCreatedAtDesc(USER_ID, CloudStatus.ACTIVE))
                .thenReturn(List.of(entities));
    }

    private void givenThoughtByIdAndUser(CloudThoughtEntity entity) {
        when(jpaRepository.findByIdAndUser_Id(THOUGHT_ID, USER_ID)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenThoughtById(CloudThoughtEntity entity) {
        when(jpaRepository.findById(THOUGHT_ID)).thenReturn(Optional.ofNullable(entity));
    }

    private CloudThoughtEntity persistedThought(Long id, CloudStatus status, boolean workedOn) {
        return CloudThoughtEntity.builder()
                .id(id)
                .user(appUserEntity(USER_ID))
                .text("hola")
                .status(status)
                .workedOn(workedOn)
                .createdAt(CREATED_AT)
                .build();
    }

    private AppUserEntity appUserEntity(Long id) {
        AppUserEntity entity = new AppUserEntity();
        entity.setId(id);
        return entity;
    }

    // --- act ---
    private CloudThought save(String text) {
        return repository.save(USER_ID, text);
    }

    private List<CloudThought> findAll() {
        return repository.findAllByUserId(USER_ID);
    }

    private Optional<CloudThought> findByIdAndUser() {
        return repository.findByIdAndUserId(THOUGHT_ID, USER_ID);
    }

    private CloudThought updateStatus(CloudStatus status) {
        return repository.updateStatus(THOUGHT_ID, status);
    }

    private void markWorkedOn() {
        repository.markWorkedOn(THOUGHT_ID);
    }

    // --- assert ---
    private void thenPersistedThoughtIsActiveAndNotWorked() {
        ArgumentCaptor<CloudThoughtEntity> captor = ArgumentCaptor.forClass(CloudThoughtEntity.class);
        verify(jpaRepository).save(captor.capture());
        CloudThoughtEntity persisted = captor.getValue();
        assertThat(persisted.getUser().getId()).isEqualTo(USER_ID);
        assertThat(persisted.getText()).isEqualTo("hola");
        assertThat(persisted.getStatus()).isEqualTo(CloudStatus.ACTIVE);
        assertThat(persisted.isWorkedOn()).isFalse();
        assertThat(persisted.getCreatedAt()).isNotNull();
    }

    private void thenThoughtMatches(CloudThought result, Long expectedId, CloudStatus status) {
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getStatus()).isEqualTo(status);
    }

    private void thenThoughtIdsAre(List<CloudThought> result, Long... ids) {
        assertThat(result).extracting(CloudThought::getId).containsExactly(ids);
    }

    private void thenThoughtPresent(Optional<CloudThought> result, Long expectedId) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(expectedId);
    }

    private void thenStatusUpdated(CloudThoughtEntity entity, CloudThought result, CloudStatus status) {
        assertThat(entity.getStatus()).isEqualTo(status);
        assertThat(result.getStatus()).isEqualTo(status);
    }

    private void thenMarkedWorkedOn(CloudThoughtEntity entity) {
        assertThat(entity.isWorkedOn()).isTrue();
        verify(jpaRepository).save(entity);
    }

    private void thenAbsent(Optional<CloudThought> result) {
        assertThat(result).isEmpty();
    }

    private void thenEmpty(List<CloudThought> result) {
        assertThat(result).isEmpty();
    }

    private void thenUpdateStatusThrowsNotFound() {
        assertThatThrownBy(() -> repository.updateStatus(THOUGHT_ID, CloudStatus.COMPLETED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CloudThought no encontrado");
    }

    private void thenMarkWorkedOnThrowsNotFound() {
        assertThatThrownBy(() -> repository.markWorkedOn(THOUGHT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CloudThought no encontrado");
    }
}
