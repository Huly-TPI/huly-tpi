package com.huly.backend.domain.useCase.BreathingSession;
import com.huly.backend.domain.model.BreathingTechnique;
import com.huly.backend.domain.repository.BreathingTechniqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBreathingTechniquesUseCase {
    private final BreathingTechniqueRepository breathingTechniqueRepository;

    public List<BreathingTechnique> execute() {
        return breathingTechniqueRepository.findAll();
    }
}