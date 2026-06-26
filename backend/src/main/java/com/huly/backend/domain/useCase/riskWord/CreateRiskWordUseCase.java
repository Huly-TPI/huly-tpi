package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.CreateRiskWordMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para la creación de una palabra de riesgo.
 * Construye el objeto de dominio con los parámetros recibidos
 * y delega la lógica de negocio al {@link RiskWordService}.
 */
@RequiredArgsConstructor
public class CreateRiskWordUseCase {

    private final RiskWordService riskWordService;
    private final CreateRiskWordMapper mapper;

    /**
     * Construye una nueva {@link RiskWord} activa y la persiste a través del servicio.
     *
     * @param request datos de la palabra de riesgo a crear
     * @return la palabra de riesgo creada con su id asignado
     */
    public CreateRiskWordResponse execute(CreateRiskWordRequest request) {
        RiskWord created = riskWordService.create(mapper.toModel(request));
        return mapper.toResponse(created);
    }
}
