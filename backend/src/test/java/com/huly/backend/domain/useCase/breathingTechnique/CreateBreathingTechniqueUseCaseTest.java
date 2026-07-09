package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.dto.breathingTechnique.CreateBreathingTechniqueRequest;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBreathingTechniqueUseCaseTest {

    @Mock
    private BreathingTechniqueRepository repository;
    @InjectMocks
    private CreateBreathingTechniqueUseCase useCase;

    @Test
    @DisplayName("Crea la técnica marcándola como activa y con los campos correctos")
    void executeShouldCreateActiveTechnique() {
        givenRepositoryEchoesSaved();

        BreathingTechnique result = execute(request());

        thenTechniqueIsActiveWithFields(result);
    }

    // --- arrange ---
    private void givenRepositoryEchoesSaved() {
        when(repository.save(any(BreathingTechnique.class))).thenAnswer(i -> i.getArgument(0));
    }

    private CreateBreathingTechniqueRequest request() {
        return new CreateBreathingTechniqueRequest("Diafragmática", "desc", 4, 0, 4, 1, 4);
    }

    // --- act ---
    private BreathingTechnique execute(CreateBreathingTechniqueRequest r) {
        return useCase.execute(r);
    }

    // --- assert ---
    private void thenTechniqueIsActiveWithFields(BreathingTechnique result) {
        assertThat(result.getName()).isEqualTo("Diafragmática");
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getInhaleSeconds()).isEqualTo(4);
        assertThat(result.getExhaleSeconds()).isEqualTo(4);
        assertThat(result.getRounds()).isEqualTo(4);
        assertThat(result.isActive()).isTrue();
        verify(repository).save(any(BreathingTechnique.class));
    }
}