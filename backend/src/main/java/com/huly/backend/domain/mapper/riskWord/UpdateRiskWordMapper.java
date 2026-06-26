package com.huly.backend.domain.mapper.riskWord;

import com.huly.backend.domain.dto.riskWord.UpdateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordResponse;
import com.huly.backend.domain.model.riskWord.RiskWord;

/**
 * Mapper de dominio para el caso de uso de actualizacion de palabra de riesgo.
 */
public class UpdateRiskWordMapper {

    public RiskWord toModel(UpdateRiskWordRequest request) {
        return RiskWord.builder()
                .word(request.word())
                .description(request.description())
                .severity(request.severity())
                .build();
    }

    public UpdateRiskWordResponse toResponse(RiskWord riskWord) {
        return new UpdateRiskWordResponse(
                riskWord.getId(),
                riskWord.getWord(),
                riskWord.getDescription(),
                riskWord.getSeverity(),
                riskWord.isActive()
        );
    }
}
