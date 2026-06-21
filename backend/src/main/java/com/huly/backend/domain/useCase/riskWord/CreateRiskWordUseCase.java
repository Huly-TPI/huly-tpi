package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
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

    /**
     * Construye una nueva {@link RiskWord} activa y la persiste a través del servicio.
     *
     * @param word        valor de la palabra de riesgo
     * @param description descripción opcional del contexto de riesgo
     * @param severity    nivel de severidad de la palabra
     * @return la palabra de riesgo creada con su id asignado
     */
    public RiskWord execute(String word, String description, RiskSeverity severity) {
        RiskWord riskWord = RiskWord.builder()
                .word(word)
                .description(description)
                .severity(severity)
                .active(true)
                .build();
        return riskWordService.create(riskWord);
    }
}
