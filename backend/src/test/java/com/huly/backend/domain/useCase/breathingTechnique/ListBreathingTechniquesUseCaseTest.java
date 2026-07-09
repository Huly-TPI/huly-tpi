package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
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
class ListBreathingTechniquesUseCaseTest {

    @Mock
    private BreathingTechniqueRepository repository;
    @InjectMocks
    private ListBreathingTechniquesUseCase useCase;

    @Test
    @DisplayName("Devuelve todas las técnicas incluidas las inactivas")
    void executeShouldReturnAll() {
        givenTechniques();

        List<BreathingTechnique> result = useCase.execute();

        thenAllReturned(result);
    }

    // --- arrange ---
    private void givenTechniques() {
        when(repository.findAll()).thenReturn(List.of(
                BreathingTechnique.builder().id(1L).name("Activa").active(true).build(),
                BreathingTechnique.builder().id(2L).name("Inactiva").active(false).build()));
    }

    // --- assert ---
    private void thenAllReturned(List<BreathingTechnique> result) {
        assertThat(result).hasSize(2);
        assertThat(result).extracting(BreathingTechnique::getName).containsExactly("Activa", "Inactiva");
        verify(repository).findAll();
    }
}