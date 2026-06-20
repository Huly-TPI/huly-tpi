package com.huly.backend.domain.useCase.BreathingSession;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
public class GetBreathingTechniquesUseCase {
    private final BreathingTechniqueRepository breathingTechniqueRepository;

    public List<BreathingTechnique> execute() {
        return breathingTechniqueRepository.findAll();
    }
}