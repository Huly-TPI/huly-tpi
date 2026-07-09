package com.huly.backend.domain.useCase.breathingTechnique;
import com.huly.backend.domain.dto.breathingTechnique.GetBreathingTechniquesResponse;
import com.huly.backend.domain.mapper.BreathingSession.GetBreathingTechniquesMapper;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBreathingTechniquesUseCase {
    private final BreathingTechniqueRepository breathingTechniqueRepository;
    private final GetBreathingTechniquesMapper mapper;

    public GetBreathingTechniquesResponse execute() {
        return mapper.toResponse(breathingTechniqueRepository.findByActive(true ));
    }
}
