package com.huly.backend.infrastructure.presentation.mapper.breathing;

import com.huly.backend.domain.dto.breathingTechnique.BreathingTechniqueItem;
import com.huly.backend.domain.dto.breathingTechnique.GetBreathingTechniquesResponse;
import com.huly.backend.infrastructure.presentation.dto.breathingTechniques.BreathingTechniqueResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper de presentacion para el feature de respiracion:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class BreathingPresentationMapper {

    public List<BreathingTechniqueResponse> toTechniqueResponses(GetBreathingTechniquesResponse response) {
        return response.techniques().stream()
                .map(this::toTechniqueResponse)
                .toList();
    }

    private BreathingTechniqueResponse toTechniqueResponse(BreathingTechniqueItem item) {
        return new BreathingTechniqueResponse(
                item.id(),
                item.name(),
                item.description(),
                item.inhaleSeconds(),
                item.holdSeconds(),
                item.exhaleSeconds(),
                item.roundsInterval(),
                item.rounds()
        );
    }
}
