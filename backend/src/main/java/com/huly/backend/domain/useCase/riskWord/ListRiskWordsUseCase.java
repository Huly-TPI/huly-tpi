package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.mapper.riskWord.ListRiskWordsMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Caso de uso para la consulta paginada y filtrada de palabras de riesgo.
 * Delega la validación de parámetros y la consulta al {@link RiskWordService}.
 */
@RequiredArgsConstructor
public class ListRiskWordsUseCase {

    private final RiskWordService riskWordService;
    private final ListRiskWordsMapper mapper;

    /**
     * Retorna una página de palabras de riesgo aplicando los filtros opcionales indicados.
     *
     * @param request pedido con los filtros y la configuración de paginación
     * @return respuesta de dominio con las palabras de riesgo que coinciden con los criterios
     */
    public ListRiskWordsResponse execute(ListRiskWordsRequest request) {
        Page<RiskWord> result = riskWordService.list(
                request.word(), request.active(), request.severity(), mapper.toPageable(request));
        return mapper.toResponse(result);
    }
}
