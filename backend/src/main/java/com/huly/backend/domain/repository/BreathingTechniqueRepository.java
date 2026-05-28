package com.huly.backend.domain.repository;
import com.huly.backend.domain.model.BreathingTechnique;
import java.util.List;

public interface BreathingTechniqueRepository {
   List<BreathingTechnique> findAll();
}