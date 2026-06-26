package com.huly.backend.domain.mapper.riskWord;

import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.dto.riskWord.RiskWordItem;
import com.huly.backend.domain.model.riskWord.RiskWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Mapper de dominio para el caso de uso de listado de palabras de riesgo.
 */
public class ListRiskWordsMapper {

    public Pageable toPageable(ListRiskWordsRequest request) {
        return PageRequest.of(request.page(), request.size());
    }

    public ListRiskWordsResponse toResponse(Page<RiskWord> page) {
        return new ListRiskWordsResponse(
                page.getContent().stream().map(this::toItem).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private RiskWordItem toItem(RiskWord riskWord) {
        return new RiskWordItem(
                riskWord.getId(),
                riskWord.getWord(),
                riskWord.getDescription(),
                riskWord.getSeverity(),
                riskWord.isActive()
        );
    }
}
