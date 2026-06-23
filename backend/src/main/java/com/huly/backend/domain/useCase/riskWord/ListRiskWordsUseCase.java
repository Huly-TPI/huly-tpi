package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Caso de uso para la consulta paginada y filtrada de palabras de riesgo.
 * Delega la validación de parámetros y la consulta al {@link RiskWordService}.
 */
@RequiredArgsConstructor
public class ListRiskWordsUseCase {

    private final RiskWordService riskWordService;

    /**
     * Retorna una página de palabras de riesgo aplicando los filtros opcionales indicados.
     *
     * @param word     fragmento para filtrar por contenido de la palabra (puede ser {@code null})
     * @param active   estado activo/inactivo para filtrar (puede ser {@code null})
     * @param severity nivel de severidad como texto para filtrar (puede ser {@code null})
     * @param pageable configuración de paginación y ordenamiento
     * @return página de palabras de riesgo que coinciden con los criterios
     */
    public Page<RiskWord> execute(String word, Boolean active, String severity, Pageable pageable) {
        return riskWordService.list(word, active, severity, pageable);
    }
}
