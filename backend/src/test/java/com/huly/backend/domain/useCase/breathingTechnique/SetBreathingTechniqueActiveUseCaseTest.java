package com.huly.backend.domain.useCase.breathingTechnique;

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
class SetBreathingTechniqueActiveUseCaseTest {

    @Mock
    private BreathingTechniqueRepository repository;
    @InjectMocks
    private SetBreathingTechniqueActiveUseCase useCase;

    @Test
    @DisplayName("Desactiva la técnica preservando los demás campos")
    void executeShouldDeactivate() {
        givenExisting(activeTechnique());
        givenRepositoryEchoesSaved();

        BreathingTechnique result = useCase.execute(1L, false);

        thenDeactivatedPreservingFields(result);
    }

    @Test
    @DisplayName("Lanza NotFound cuando la técnica no existe")
    void executeShouldThrowWhenNotFound() {
        givenNoExisting();

        assertThatThrownBy(() -> useCase.execute(1L, false)).isInstanceOf(NotFoundException.class);
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

    private BreathingTechnique activeTechnique() {
        return BreathingTechnique.builder().id(1L).name("Diafragmática").description("d")
                .inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4)
                .active(true).build();
    }

    // --- assert ---
    private void thenDeactivatedPreservingFields(BreathingTechnique result) {
        assertThat(result.getName()).isEqualTo("Diafragmática");
        assertThat(result.getRounds()).isEqualTo(4);
        assertThat(result.isActive()).isFalse();
    }
}