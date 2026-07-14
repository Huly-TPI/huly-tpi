package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.dto.breathingTechnique.UpdateBreathingTechniqueRequest;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBreathingTechniqueUseCaseTest {

    @Mock
    private BreathingTechniqueRepository repository;
    @InjectMocks
    private UpdateBreathingTechniqueUseCase useCase;

    @Test
    @DisplayName("Actualiza los campos preservando id y estado activo existente")
    void executeShouldUpdatePreservingActive() {
        givenExisting(existingInactive());
        givenRepositoryEchoesSaved();

        BreathingTechnique result = execute(updateRequest());

        thenFieldsUpdatedAndActivePreserved(result);
    }

    @Test
    @DisplayName("Lanza NotFound cuando la técnica no existe")
    void executeShouldThrowWhenNotFound() {
        givenNoExisting();

        thenThrowsNotFound();
    }

    // --- arrange ---
    private void givenExisting(BreathingTechnique t) {
        when(repository.findById(1L)).thenReturn(Optional.of(t));
    }

    private void givenNoExisting() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
    }

    private void givenRepositoryEchoesSaved() {
        when(repository.save(any(BreathingTechnique.class))).thenAnswer(i -> i.getArgument(0));
    }

    private BreathingTechnique existingInactive() {
        return BreathingTechnique.builder().id(1L).name("Old").description("old")
                .inhaleSeconds(1).holdSeconds(1).exhaleSeconds(1).roundsInterval(1).rounds(1)
                .active(false).build();
    }

    private UpdateBreathingTechniqueRequest updateRequest() {
        return new UpdateBreathingTechniqueRequest(1L, "Cuadrada", "nueva", 4, 4, 4, 2, 5);
    }

    // --- act ---
    private BreathingTechnique execute(UpdateBreathingTechniqueRequest r) {
        return useCase.execute(r);
    }

    // --- assert ---
    private void thenFieldsUpdatedAndActivePreserved(BreathingTechnique result) {
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Cuadrada");
        assertThat(result.getExhaleSeconds()).isEqualTo(4);
        assertThat(result.getRounds()).isEqualTo(5);
        assertThat(result.isActive()).isFalse();
    }

    private void thenThrowsNotFound() {
        assertThatThrownBy(() -> useCase.execute(updateRequest()))
                .isInstanceOf(NotFoundException.class);
    }
}