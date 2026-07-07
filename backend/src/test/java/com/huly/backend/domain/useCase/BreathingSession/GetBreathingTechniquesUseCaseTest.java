package com.huly.backend.domain.useCase.BreathingSession;

import com.huly.backend.domain.dto.BreathingSession.GetBreathingTechniquesResponse;
import com.huly.backend.domain.mapper.BreathingSession.GetBreathingTechniquesMapper;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBreathingTechniquesUseCaseTest {

    private static final Long ID_ONE = 1L;
    private static final Long ID_TWO = 2L;
    private static final String NAME_ONE = "Diafragmática";
    private static final String NAME_TWO = "Cuadrada";

    @Mock
    private BreathingTechniqueRepository breathingTechniqueRepository;

    private GetBreathingTechniquesUseCase getBreathingTechniquesUseCase;

    @BeforeEach
    void setUp() {
        getBreathingTechniquesUseCase = new GetBreathingTechniquesUseCase(
                breathingTechniqueRepository, new GetBreathingTechniquesMapper());
    }

    @Test
    @DisplayName("Devuelve todas las técnicas cuando el repositorio tiene datos")
    void executeShouldReturnAllTechniquesWhenRepositoryHasData() {
        // --- arrange ---
        givenTwoConfiguredTechniques();

        // --- act ---
        GetBreathingTechniquesResponse result = getTechniques();

        // --- assert ---
        thenAllTechniquesAreReturned(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el repositorio no tiene datos")
    void executeShouldReturnEmptyListWhenRepositoryHasNoData() {
        // --- arrange ---
        givenNoConfiguredTechniques();

        // --- act ---
        GetBreathingTechniquesResponse result = getTechniques();

        // --- assert ---
        thenNoTechniquesAreReturned(result);
    }

    @Test
    @DisplayName("Mapea todos los campos de la técnica al ítem de la respuesta")
    void executeShouldMapAllTechniqueFieldsWhenRepositoryHasData() {
        // --- arrange ---
        givenSingleFullyDetailedTechnique();

        // --- act ---
        GetBreathingTechniquesResponse result = getTechniques();

        // --- assert ---
        thenTechniqueFieldsAreMapped(result);
    }

    // --- arrange ---

    private void givenTwoConfiguredTechniques() {
        List<BreathingTechnique> techniques = List.of(
                BreathingTechnique.builder().id(ID_ONE).name(NAME_ONE).inhaleSeconds(4).holdSeconds(0)
                        .exhaleSeconds(4).roundsInterval(1).rounds(4).build(),
                BreathingTechnique.builder().id(ID_TWO).name(NAME_TWO).inhaleSeconds(4).holdSeconds(4)
                        .exhaleSeconds(4).roundsInterval(1).rounds(4).build());
        when(breathingTechniqueRepository.findAll()).thenReturn(techniques);
    }

    private void givenNoConfiguredTechniques() {
        when(breathingTechniqueRepository.findAll()).thenReturn(List.of());
    }

    private void givenSingleFullyDetailedTechnique() {
        BreathingTechnique technique = BreathingTechnique.builder()
                .id(ID_ONE)
                .name(NAME_ONE)
                .description("Respiración profunda y pausada")
                .inhaleSeconds(4)
                .holdSeconds(2)
                .exhaleSeconds(6)
                .roundsInterval(3)
                .rounds(5)
                .build();
        when(breathingTechniqueRepository.findAll()).thenReturn(List.of(technique));
    }

    // --- act ---

    private GetBreathingTechniquesResponse getTechniques() {
        return getBreathingTechniquesUseCase.execute();
    }

    // --- assert ---

    private void thenAllTechniquesAreReturned(GetBreathingTechniquesResponse result) {
        assertThat(result.techniques()).hasSize(2);
        assertThat(result.techniques()).extracting("id").containsExactly(ID_ONE, ID_TWO);
        assertThat(result.techniques()).extracting("name").containsExactly(NAME_ONE, NAME_TWO);
        verify(breathingTechniqueRepository).findAll();
    }

    private void thenNoTechniquesAreReturned(GetBreathingTechniquesResponse result) {
        assertThat(result.techniques()).isEmpty();
        verify(breathingTechniqueRepository).findAll();
    }

    private void thenTechniqueFieldsAreMapped(GetBreathingTechniquesResponse result) {
        assertThat(result.techniques()).hasSize(1);
        assertThat(result.techniques().get(0).id()).isEqualTo(ID_ONE);
        assertThat(result.techniques().get(0).name()).isEqualTo(NAME_ONE);
        assertThat(result.techniques().get(0).description()).isEqualTo("Respiración profunda y pausada");
        assertThat(result.techniques().get(0).inhaleSeconds()).isEqualTo(4);
        assertThat(result.techniques().get(0).holdSeconds()).isEqualTo(2);
        assertThat(result.techniques().get(0).exhaleSeconds()).isEqualTo(6);
        assertThat(result.techniques().get(0).roundsInterval()).isEqualTo(3);
        assertThat(result.techniques().get(0).rounds()).isEqualTo(5);
        verify(breathingTechniqueRepository).findAll();
    }
}
