package com.huly.backend.domain.mapper.riskWord;

import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.model.riskWord.RiskWord;

/**
 * Mapper de dominio para el caso de uso de creacion de palabra de riesgo.
 */
public class CreateRiskWordMapper {

    public RiskWord toModel(CreateRiskWordRequest request) {
        return RiskWord.builder()
                .word(request.word())
                .description(request.description())
                .severity(request.severity())
                .active(true)
                .build();
    }

    public CreateRiskWordResponse toResponse(RiskWord riskWord) {
        return new CreateRiskWordResponse(
                riskWord.getId(),
                riskWord.getWord(),
                riskWord.getDescription(),
                riskWord.getSeverity(),
                riskWord.isActive()
        );
    }
}
