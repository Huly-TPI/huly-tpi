package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.DeleteRiskWordRequest;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para la eliminación de una palabra de riesgo.
 * Delega directamente al {@link RiskWordService}, que verifica
 * la existencia del registro antes de eliminarlo.
 */
@RequiredArgsConstructor
public class DeleteRiskWordUseCase {

    private final RiskWordService riskWordService;

    /**
     * Elimina la palabra de riesgo correspondiente al pedido indicado.
     *
     * @param request pedido con el identificador de la palabra de riesgo a eliminar
     */
    public void execute(DeleteRiskWordRequest request) {
        riskWordService.delete(request.id());
    }
}
