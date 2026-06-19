package com.huly.backend.domain.repository.breathingTechnique;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import java.util.List;

public interface BreathingTechniqueRepository {
   List<BreathingTechnique> findAll();
}