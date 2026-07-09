package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class ListBreathingTechniquesUseCase {

    private final BreathingTechniqueRepository repository;

    public List<BreathingTechnique> execute() {
        return repository.findAll();
    }
}