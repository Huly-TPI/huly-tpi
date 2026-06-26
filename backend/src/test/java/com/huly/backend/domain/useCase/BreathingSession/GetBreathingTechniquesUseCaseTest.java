package com.huly.backend.domain.useCase.BreathingSession;
import com.huly.backend.domain.dto.BreathingSession.GetBreathingTechniquesResponse;
import com.huly.backend.domain.mapper.BreathingSession.GetBreathingTechniquesMapper;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private BreathingTechniqueRepository breathingTechniqueRepository;

    private GetBreathingTechniquesUseCase getBreathingTechniquesUseCase;

    @BeforeEach
    void setUp() {
        getBreathingTechniquesUseCase = new GetBreathingTechniquesUseCase(
                breathingTechniqueRepository, new GetBreathingTechniquesMapper());
    }

    @Test
    void execute_shouldReturnAllTechniques_whenRepositoryHasData(){
        List<BreathingTechnique> techniques = List.of(
       BreathingTechnique.builder().id(1L).name("Diafragmática").inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4).build(),
            BreathingTechnique.builder().id(2L).name("Cuadrada").inhaleSeconds(4).holdSeconds(4).exhaleSeconds(4).roundsInterval(1).rounds(4).build()
        );
        when(breathingTechniqueRepository.findAll()).thenReturn(techniques);
        GetBreathingTechniquesResponse result = getBreathingTechniquesUseCase.execute();
        assertThat(result.techniques()).hasSize(2);
        assertThat(result.techniques()).extracting("id").containsExactly(1L, 2L);
        assertThat(result.techniques()).extracting("name").containsExactly("Diafragmática", "Cuadrada");
        verify(breathingTechniqueRepository).findAll();
    }

    @Test
    void execute_shouldReturnEmptyList_whenRepositoryHasNoData() {
        when(breathingTechniqueRepository.findAll()).thenReturn(List.of());
        GetBreathingTechniquesResponse result = getBreathingTechniquesUseCase.execute();
        assertThat(result.techniques()).isEmpty();
        verify(breathingTechniqueRepository).findAll();
    }
     }
