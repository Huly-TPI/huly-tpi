package com.huly.backend.domain.useCase.BreathingSession;
import com.huly.backend.domain.model.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
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
class GetBreathingTechniquesUseCaseTest {
    
    @Mock
    private BreathingTechniqueRepository breathingTechniqueRepository;

    @InjectMocks
    private GetBreathingTechniquesUseCase getBreathingTechniquesUseCase;

    @Test
    void execute_shouldReturnAllTechniques_whenRepositoryHasData(){ 
        List<BreathingTechnique> techniques = List.of(
       BreathingTechnique.builder().id(1L).name("Diafragmática").inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4).build(),
            BreathingTechnique.builder().id(2L).name("Cuadrada").inhaleSeconds(4).holdSeconds(4).exhaleSeconds(4).roundsInterval(1).rounds(4).build()
        );
        when(breathingTechniqueRepository.findAll()).thenReturn(techniques);
        List<BreathingTechnique> result = getBreathingTechniquesUseCase.execute();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(techniques);
        verify(breathingTechniqueRepository).findAll();
    }

    @Test 
    void execute_shouldReturnEmptyList_whenRepositoryHasNoData() {
        when(breathingTechniqueRepository.findAll()).thenReturn(List.of());
        List<BreathingTechnique> result = getBreathingTechniquesUseCase.execute();
        assertThat(result).isEmpty();
        verify(breathingTechniqueRepository).findAll();
    }
     }