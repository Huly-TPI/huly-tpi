package com.huly.backend.domain.repository.breathingTechnique;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import java.util.List;
import java.util.Optional;

public interface BreathingTechniqueRepository {
   List<BreathingTechnique> findAll();
   List<BreathingTechnique> findByActive(boolean active);
   Optional<BreathingTechnique> findById(Long id);
   BreathingTechnique save(BreathingTechnique technique);
}