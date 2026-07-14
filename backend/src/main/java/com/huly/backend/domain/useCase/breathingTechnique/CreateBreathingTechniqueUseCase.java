package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.dto.breathingTechnique.CreateBreathingTechniqueRequest;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateBreathingTechniqueUseCase {

    private final BreathingTechniqueRepository repository;

    public BreathingTechnique execute(CreateBreathingTechniqueRequest r) {
        return repository.save(BreathingTechnique.builder()
                .name(r.name()).description(r.description())
                .inhaleSeconds(r.inhaleSeconds()).holdSeconds(r.holdSeconds())
                .exhaleSeconds(r.exhaleSeconds()).roundsInterval(r.roundsInterval())
                .rounds(r.rounds())
                .active(true)
                .build());
    }
}