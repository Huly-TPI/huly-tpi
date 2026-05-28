package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.BreathingTechnique;
import com.huly.backend.infrastructure.repository.entity.BreathingTechniquesEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBreathingTechniqueJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BreathingTechniqueRepositoryImplTest {

    @Mock
    private IBreathingTechniqueJpaRepository jpaRepository; 

    @InjectMocks
    private BreathingTechniqueRepositoryImpl repository;

    @Test
    void findAll_shouldReturnListOfTechniques() {
        List<BreathingTechniquesEntity> entities = List.of(
                BreathingTechniquesEntity.builder().id(1L).name("Diafragmática").inhaleSeconds(4).holdSeconds(0).exhaleSeconds(4).roundsInterval(1).rounds(4).build(),
                BreathingTechniquesEntity.builder().id(2L).name("Cuadrada").inhaleSeconds(4).holdSeconds(4).exhaleSeconds(4).roundsInterval(1).rounds(4).build()
        );
        when(jpaRepository.findAll()).thenReturn(entities);
        List<BreathingTechnique> result = repository.findAll();
        assertEquals(2, result.size());
        assertEquals("Diafragmática", result.get(0).getName());
        assertEquals("Cuadrada", result.get(1).getName());
        verify(jpaRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoTechniques() {
        when(jpaRepository.findAll()).thenReturn(List.of());
        List<BreathingTechnique> result = repository.findAll();
        assertTrue(result.isEmpty());
        verify(jpaRepository).findAll();
    }

 }