package com.huly.backend.domain.mapper.BreathingSession;

import com.huly.backend.domain.dto.breathingTechnique.BreathingTechniqueItem;
import com.huly.backend.domain.dto.breathingTechnique.GetBreathingTechniquesResponse;
import com.huly.backend.domain.model.breathingTechnique.BreathingTechnique;

import java.util.List;

/**
 * Mapper de dominio para el caso de uso de listado de tecnicas de respiracion.
 */
public class GetBreathingTechniquesMapper {

    public GetBreathingTechniquesResponse toResponse(List<BreathingTechnique> techniques) {
        List<BreathingTechniqueItem> items = techniques.stream()
                .map(this::toItem)
                .toList();
        return new GetBreathingTechniquesResponse(items);
    }

    private BreathingTechniqueItem toItem(BreathingTechnique technique) {
        return new BreathingTechniqueItem(
                technique.getId(),
                technique.getName(),
                technique.getDescription(),
                technique.getInhaleSeconds(),
                technique.getHoldSeconds(),
                technique.getExhaleSeconds(),
                technique.getRoundsInterval(),
                technique.getRounds()
        );
    }
}
