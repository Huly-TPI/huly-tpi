package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.infrastructure.repository.entity.BreathingTechniquesEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBreathingTechniqueJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BreathingTechniqueRepositoryImplTest {

    @Mock
    private IBreathingTechniqueJpaRepository jpaRepository;

    @InjectMocks
    private BreathingTechniqueRepositoryImpl repository;

    @Test
    @DisplayName("Devuelve la lista de técnicas de respiración")
    void findAllShouldReturnListOfTechniques() {
        givenTechniques(
                techniqueEntity(1L, "Diafragmática", 4, 0, 4),
                techniqueEntity(2L, "Cuadrada", 4, 4, 4));

        List<BreathingTechnique> result = findAll();

        thenTechniquesMapped(result);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay técnicas")
    void findAllShouldReturnEmptyListWhenNoTechniques() {
        givenTechniques();

        List<BreathingTechnique> result = findAll();

        thenEmpty(result);
    }

    @Test
    @DisplayName("Guarda una técnica nueva mapeando dominio a entidad y de vuelta")
    void saveShouldMapNewTechnique() {
        givenSavedEntityReturns(techniqueEntity(1L, "Diafragmática", 4, 0, 4));

        BreathingTechnique result = save(newDomain());

        thenSavedMapped(result);
    }

    @Test
    @DisplayName("Al guardar con id existente trae la entidad y la guarda")
    void saveShouldFetchAndUpdateExisting() {
        givenTechniqueById(5L, techniqueEntity(5L, "Old", 1, 1, 1));
        givenSaveEchoesEntity();

        BreathingTechnique result = save(domainWithId(5L, "Nuevo"));

        thenUpdatedByIdSaved(result);
    }

    @Test
    @DisplayName("Devuelve las técnicas activas mapeadas")
    void findByActiveShouldReturnMapped() {
        givenActiveTechniques(techniqueEntity(1L, "Diafragmática", 4, 0, 4));

        List<BreathingTechnique> result = repository.findByActive(true);

        thenSingleActiveMapped(result);
    }

    @Test
    @DisplayName("Devuelve la técnica por id mapeada")
    void findByIdShouldReturnMapped() {
        givenTechniqueById(10L, techniqueEntity(10L, "Cuadrada", 4, 4, 4));

        Optional<BreathingTechnique> result = repository.findById(10L);

        thenTechniqueFoundById(result);
    }

    // --- arrange ---
    private void givenTechniques(BreathingTechniquesEntity... entities) {
        when(jpaRepository.findAll()).thenReturn(List.of(entities));
    }

    private void givenSavedEntityReturns(BreathingTechniquesEntity entity) {
        when(jpaRepository.save(any(BreathingTechniquesEntity.class))).thenReturn(entity);
    }

    private void givenSaveEchoesEntity() {
        when(jpaRepository.save(any(BreathingTechniquesEntity.class))).thenAnswer(i -> i.getArgument(0));
    }

    private void givenActiveTechniques(BreathingTechniquesEntity... entities) {
        when(jpaRepository.findByActive(true)).thenReturn(List.of(entities));
    }

    private void givenTechniqueById(Long id, BreathingTechniquesEntity entity) {
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));
    }

    private BreathingTechniquesEntity techniqueEntity(Long id, String name, int inhale, int hold, int exhale) {
        return BreathingTechniquesEntity.builder()
                .id(id).name(name)
                .inhaleSeconds(inhale).holdSeconds(hold).exhaleSeconds(exhale)
                .roundsInterval(1).rounds(4)
                .build();
    }

    private BreathingTechnique newDomain() {
        return BreathingTechnique.builder().name("Diafragmática").description("d")
                .inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4)
                .active(true).build();
    }

    private BreathingTechnique domainWithId(Long id, String name) {
        return BreathingTechnique.builder().id(id).name(name).description("d")
                .inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4)
                .active(true).build();
    }

    // --- act ---
    private List<BreathingTechnique> findAll() {
        return repository.findAll();
    }

    private BreathingTechnique save(BreathingTechnique t) {
        return repository.save(t);
    }

    // --- assert ---
    private void thenTechniquesMapped(List<BreathingTechnique> result) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Diafragmática");
        assertThat(result.get(1).getName()).isEqualTo("Cuadrada");
        verify(jpaRepository).findAll();
    }

    private void thenEmpty(List<BreathingTechnique> result) {
        assertThat(result).isEmpty();
        verify(jpaRepository).findAll();
    }

    private void thenSavedMapped(BreathingTechnique result) {
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Diafragmática");
        verify(jpaRepository).save(any(BreathingTechniquesEntity.class));
    }

    private void thenUpdatedByIdSaved(BreathingTechnique result) {
        assertThat(result.getName()).isEqualTo("Nuevo");
        verify(jpaRepository).findById(5L);
        verify(jpaRepository).save(any(BreathingTechniquesEntity.class));
    }

    private void thenSingleActiveMapped(List<BreathingTechnique> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Diafragmática");
        verify(jpaRepository).findByActive(true);
    }

    private void thenTechniqueFoundById(Optional<BreathingTechnique> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getName()).isEqualTo("Cuadrada");
    }
}