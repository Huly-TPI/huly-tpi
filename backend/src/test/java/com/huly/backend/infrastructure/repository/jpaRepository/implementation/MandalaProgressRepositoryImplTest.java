package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.mandala.MandalaProgress;
import com.huly.backend.infrastructure.repository.entity.MandalaProgressEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IMandalaProgressJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MandalaProgressRepositoryImplTest {

    private static final Long USER_ID = 7L;
    private static final String MANDALA_ID = "mandala-01";

    @Mock
    private IMandalaProgressJpaRepository jpaRepository;

    @InjectMocks
    private MandalaProgressRepositoryImpl repository;

    @Test
    @DisplayName("Crea y guarda la entidad cuando el progreso es nuevo")
    void saveNewProgressCreatesAndSavesEntity() {
        givenNoProgress();
        givenSaved(entity("paint".getBytes(), true));

        MandalaProgress result = save(progress("paint".getBytes()));

        thenCreatedEntityMatches();
        thenResultMatches(result);
    }

    @Test
    @DisplayName("Actualiza y guarda la entidad cuando el progreso ya existe")
    void saveExistingProgressUpdatesAndSavesEntity() {
        MandalaProgressEntity existingEntity = entity("old-paint".getBytes(), false);
        givenExistingProgress(existingEntity);
        givenSaved(existingEntity);

        save(progress("new-paint".getBytes()));

        thenExistingEntitySavedWithNewPaint(existingEntity);
    }

    @Test
    @DisplayName("Devuelve el dominio mapeado al buscar por usuario y mandala")
    void findByUserIdAndMandalaIdShouldReturnMappedDomain() {
        givenExistingProgress(entity("paint".getBytes(), true));

        Optional<MandalaProgress> result = findByUserIdAndMandalaId();

        thenMappedDomain(result);
    }

    @Test
    @DisplayName("Actualiza el flag de sesión cuando el progreso existe")
    void markSessionRegisteredExistingProgressUpdatesFlag() {
        MandalaProgressEntity entity = entity(null, false);
        givenExistingProgress(entity);

        markSessionRegistered();

        thenSessionFlagSaved(entity);
    }

    @Test
    @DisplayName("Delega la eliminación en el repositorio JPA")
    void deleteByUserIdAndMandalaIdShouldDelegateToJpaRepository() {
        deleteByUserIdAndMandalaId();

        thenDeleteDelegated();
    }

    // --- arrange ---
    private void givenNoProgress() {
        when(jpaRepository.findByUserIdAndMandalaId(USER_ID, MANDALA_ID)).thenReturn(Optional.empty());
    }

    private void givenExistingProgress(MandalaProgressEntity entity) {
        when(jpaRepository.findByUserIdAndMandalaId(USER_ID, MANDALA_ID)).thenReturn(Optional.of(entity));
    }

    private void givenSaved(MandalaProgressEntity entity) {
        when(jpaRepository.save(any(MandalaProgressEntity.class))).thenReturn(entity);
    }

    private MandalaProgress progress(byte[] paintBlob) {
        return MandalaProgress.builder()
                .userId(USER_ID)
                .mandalaId(MANDALA_ID)
                .paintBlob(paintBlob)
                .sessionRegistered(true)
                .build();
    }

    private MandalaProgressEntity entity(byte[] paintBlob, boolean sessionRegistered) {
        return MandalaProgressEntity.builder()
                .userId(USER_ID)
                .mandalaId(MANDALA_ID)
                .paintBlob(paintBlob)
                .sessionRegistered(sessionRegistered)
                .build();
    }

    // --- act ---
    private MandalaProgress save(MandalaProgress progress) {
        return repository.save(progress);
    }

    private Optional<MandalaProgress> findByUserIdAndMandalaId() {
        return repository.findByUserIdAndMandalaId(USER_ID, MANDALA_ID);
    }

    private void markSessionRegistered() {
        repository.markSessionRegistered(USER_ID, MANDALA_ID);
    }

    private void deleteByUserIdAndMandalaId() {
        repository.deleteByUserIdAndMandalaId(USER_ID, MANDALA_ID);
    }

    // --- assert ---
    private void thenCreatedEntityMatches() {
        ArgumentCaptor<MandalaProgressEntity> captor = ArgumentCaptor.forClass(MandalaProgressEntity.class);
        verify(jpaRepository).save(captor.capture());
        MandalaProgressEntity persisted = captor.getValue();
        assertThat(persisted.getUserId()).isEqualTo(7L);
        assertThat(persisted.getMandalaId()).isEqualTo("mandala-01");
        assertThat(persisted.getPaintBlob()).isEqualTo("paint".getBytes());
        assertThat(persisted.isSessionRegistered()).isTrue();
    }

    private void thenResultMatches(MandalaProgress result) {
        assertThat(result.getUserId()).isEqualTo(7L);
        assertThat(result.getMandalaId()).isEqualTo("mandala-01");
    }

    private void thenExistingEntitySavedWithNewPaint(MandalaProgressEntity existingEntity) {
        verify(jpaRepository).save(existingEntity);
        assertThat(existingEntity.getPaintBlob()).isEqualTo("new-paint".getBytes());
    }

    private void thenMappedDomain(Optional<MandalaProgress> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(7L);
        assertThat(result.get().getMandalaId()).isEqualTo("mandala-01");
        assertThat(result.get().getPaintBlob()).isEqualTo("paint".getBytes());
        assertThat(result.get().isSessionRegistered()).isTrue();
    }

    private void thenSessionFlagSaved(MandalaProgressEntity entity) {
        verify(jpaRepository).save(entity);
        assertThat(entity.isSessionRegistered()).isTrue();
    }

    private void thenDeleteDelegated() {
        verify(jpaRepository).deleteByUserIdAndMandalaId(7L, "mandala-01");
    }
}
