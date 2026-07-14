package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.dto.breathingTechnique.UpdateBreathingTechniqueRequest;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateBreathingTechniqueUseCase {

    private final BreathingTechniqueRepository repository;

    public BreathingTechnique execute(UpdateBreathingTechniqueRequest r) {
        BreathingTechnique existing = repository.findById(r.id())
                .orElseThrow(() -> new NotFoundException("Técnica de respiración no encontrada " + r.id()));
        return repository.save(BreathingTechnique.builder()
                .id(existing.getId())
                .name(r.name()).description(r.description())
                .inhaleSeconds(r.inhaleSeconds()).holdSeconds(r.holdSeconds())
                .exhaleSeconds(r.exhaleSeconds()).roundsInterval(r.roundsInterval())
                .rounds(r.rounds())
                .active(existing.isActive())
                .build());
    }
}