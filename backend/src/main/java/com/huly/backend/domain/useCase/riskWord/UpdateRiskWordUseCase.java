package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.UpdateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.UpdateRiskWordMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para la actualización de una palabra de riesgo existente.
 * Construye el objeto de dominio con los nuevos valores y delega
 * la lógica de negocio al {@link RiskWordService}.
 */
@RequiredArgsConstructor
public class UpdateRiskWordUseCase {

    private final RiskWordService riskWordService;
    private final UpdateRiskWordMapper mapper;

    /**
     * Construye el objeto de dominio con los nuevos datos y lo pasa al servicio para su actualización.
     *
     * @param request datos de la palabra de riesgo a actualizar
     * @return la palabra de riesgo actualizada
     */
    public UpdateRiskWordResponse execute(UpdateRiskWordRequest request) {
        RiskWord updated = riskWordService.update(request.id(), mapper.toModel(request));
        return mapper.toResponse(updated);
    }
}
