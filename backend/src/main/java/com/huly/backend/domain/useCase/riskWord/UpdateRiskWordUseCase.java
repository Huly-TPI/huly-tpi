package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
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

    /**
     * Construye el objeto de dominio con los nuevos datos y lo pasa al servicio para su actualización.
     *
     * @param id          identificador de la palabra de riesgo a actualizar
     * @param word        nuevo valor de la palabra
     * @param description nueva descripción (puede ser {@code null})
     * @param severity    nuevo nivel de severidad
     * @return la palabra de riesgo actualizada
     */
    public RiskWord execute(Long id, String word, String description, RiskSeverity severity) {
        RiskWord riskWord = RiskWord.builder()
                .word(word)
                .description(description)
                .severity(severity)
                .build();
        return riskWordService.update(id, riskWord);
    }
}
