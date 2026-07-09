package com.huly.backend.domain.useCase.breathingTechnique;

import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;
import com.huly.backend.domain.repository.breathingTechnique.BreathingTechniqueRepository;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetBreathingTechniqueActiveUseCase {

    private final BreathingTechniqueRepository repository;

    public BreathingTechnique execute(Long id, boolean active) {
        BreathingTechnique t = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Técnica de respiración no encontrada " + id));
        return repository.save(BreathingTechnique.builder()
                .id(t.getId())
                .name(t.getName()).description(t.getDescription())
                .inhaleSeconds(t.getInhaleSeconds()).holdSeconds(t.getHoldSeconds())
                .exhaleSeconds(t.getExhaleSeconds()).roundsInterval(t.getRoundsInterval())
                .rounds(t.getRounds())
                .active(active)
                .build());
    }
}