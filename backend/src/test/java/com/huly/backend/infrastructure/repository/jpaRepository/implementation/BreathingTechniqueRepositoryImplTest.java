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

import static org.assertj.core.api.Assertions.assertThat;
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

    // --- arrange ---
    private void givenTechniques(BreathingTechniquesEntity... entities) {
        when(jpaRepository.findAll()).thenReturn(List.of(entities));
    }

    private BreathingTechniquesEntity techniqueEntity(Long id, String name, int inhale, int hold, int exhale) {
        return BreathingTechniquesEntity.builder()
                .id(id).name(name)
                .inhaleSeconds(inhale).holdSeconds(hold).exhaleSeconds(exhale)
                .roundsInterval(1).rounds(4)
                .build();
    }

    // --- act ---
    private List<BreathingTechnique> findAll() {
        return repository.findAll();
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
}
