package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IActivityJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.ActivityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityRepositoryImplTest {

    @Mock
    private IActivityJpaRepository jpaRepository;

    @Spy
    private ActivityMapper activityMapper = new ActivityMapper();

    private ActivityRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ActivityRepositoryImpl(jpaRepository, activityMapper);
    }

    @Test
    @DisplayName("Devuelve la lista de dominio mapeada")
    void findAllShouldReturnMappedDomainList() {
        givenAllActivities(
                activityEntity(1L, ActivityType.BREATHING, "Respirar"),
                activityEntity(2L, ActivityType.DIARY, "Diario"));

        List<Activity> result = findAll();

        thenActivitiesMapped(result);
    }

    @Test
    @DisplayName("Delega la existencia por id en el repositorio JPA")
    void existsByIdShouldCallJpaRepository() {
        givenExists(1L, true);
        givenExists(2L, false);

        boolean existing = existsById(1L);
        boolean missing = existsById(2L);

        thenExists(existing, missing);
    }

    @Test
    @DisplayName("Devuelve el dominio opcional según exista el id")
    void findByIdShouldReturnOptionalDomain() {
        givenActivityById(1L, activityEntity(1L, ActivityType.BREATHING, "Respirar"));
        givenActivityByIdMissing(2L);

        Optional<Activity> present = findById(1L);
        Optional<Activity> absent = findById(2L);

        thenActivityFoundAndMissing(present, absent);
    }

    @Test
    @DisplayName("Mapea, guarda y devuelve el dominio al guardar")
    void saveShouldMapAndSaveAndReturnDomain() {
        givenSaved(activityEntity(1L, ActivityType.BREATHING, "Respirar"));

        Activity saved = save(activity(1L, ActivityType.BREATHING, "Respirar"));

        thenActivitySaved(saved);
    }

    // --- arrange ---
    private void givenAllActivities(ActivityEntity... entities) {
        when(jpaRepository.findAll()).thenReturn(List.of(entities));
    }

    private void givenExists(Long id, boolean exists) {
        when(jpaRepository.existsById(id)).thenReturn(exists);
    }

    private void givenActivityById(Long id, ActivityEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    private void givenActivityByIdMissing(Long id) {
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenSaved(ActivityEntity entity) {
        when(jpaRepository.save(any(ActivityEntity.class))).thenReturn(entity);
    }

    private ActivityEntity activityEntity(Long id, ActivityType type, String title) {
        return ActivityEntity.builder().id(id).type(type).title(title).build();
    }

    private Activity activity(Long id, ActivityType type, String title) {
        return Activity.builder().id(id).type(type).title(title).build();
    }

    // --- act ---
    private List<Activity> findAll() {
        return repository.findAll();
    }

    private boolean existsById(Long id) {
        return repository.existsById(id);
    }

    private Optional<Activity> findById(Long id) {
        return repository.findById(id);
    }

    private Activity save(Activity activity) {
        return repository.save(activity);
    }

    // --- assert ---
    private void thenActivitiesMapped(List<Activity> result) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Respirar");
        assertThat(result.get(1).getTitle()).isEqualTo("Diario");
        verify(jpaRepository).findAll();
    }

    private void thenExists(boolean existing, boolean missing) {
        assertThat(existing).isTrue();
        assertThat(missing).isFalse();
        verify(jpaRepository).existsById(1L);
        verify(jpaRepository).existsById(2L);
    }

    private void thenActivityFoundAndMissing(Optional<Activity> present, Optional<Activity> absent) {
        assertThat(present).isPresent();
        assertThat(present.get().getTitle()).isEqualTo("Respirar");
        assertThat(absent).isEmpty();
        verify(jpaRepository).findById(1L);
        verify(jpaRepository).findById(2L);
    }

    private void thenActivitySaved(Activity saved) {
        assertThat(saved.getTitle()).isEqualTo("Respirar");
        verify(jpaRepository).save(any(ActivityEntity.class));
    }
}
