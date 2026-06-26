package com.huly.backend.domain.useCase.BreathingSession;
import com.huly.backend.domain.dto.BreathingSession.GetBreathingTechniquesResponse;
import com.huly.backend.domain.mapper.BreathingSession.GetBreathingTechniquesMapper;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBreathingTechniquesUseCase {
    private final BreathingTechniqueRepository breathingTechniqueRepository;
    private final GetBreathingTechniquesMapper mapper;

    public GetBreathingTechniquesResponse execute() {
        return mapper.toResponse(breathingTechniqueRepository.findAll());
    }
}
