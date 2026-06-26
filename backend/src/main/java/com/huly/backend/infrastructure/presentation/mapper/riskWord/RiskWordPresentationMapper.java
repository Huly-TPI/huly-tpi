package com.huly.backend.infrastructure.presentation.mapper.riskWord;

import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.dto.riskWord.DeleteRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.dto.riskWord.RiskWordItem;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordResponse;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordPageResponse;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordRequest;
import com.huly.backend.infrastructure.presentation.dto.riskWord.RiskWordResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper de presentacion para el feature de palabras de riesgo:
 * traduce entre los DTOs web y los DTOs de dominio.
 */
@Component
public class RiskWordPresentationMapper {

    public CreateRiskWordRequest toCreateRequest(RiskWordRequest request) {
        return new CreateRiskWordRequest(request.word(), request.description(), request.severity());
    }

    public UpdateRiskWordRequest toUpdateRequest(Long id, RiskWordRequest request) {
        return new UpdateRiskWordRequest(id, request.word(), request.description(), request.severity());
    }

    public DeleteRiskWordRequest toDeleteRequest(Long id) {
        return new DeleteRiskWordRequest(id);
    }

    public ListRiskWordsRequest toListRequest(String word, Boolean active, String severity, int page, int size) {
        return new ListRiskWordsRequest(word, active, severity, page, size);
    }

    public RiskWordResponse toResponse(CreateRiskWordResponse response) {
        return new RiskWordResponse(
                response.id(),
                response.word(),
                response.description(),
                severityToText(response.severity()),
                response.active()
        );
    }

    public RiskWordResponse toResponse(UpdateRiskWordResponse response) {
        return new RiskWordResponse(
                response.id(),
                response.word(),
                response.description(),
                severityToText(response.severity()),
                response.active()
        );
    }

    public RiskWordPageResponse toPageResponse(ListRiskWordsResponse response) {
        return new RiskWordPageResponse(
                response.content().stream().map(this::toResponse).toList(),
                response.pageNumber(),
                response.pageSize(),
                response.totalElements(),
                response.totalPages(),
                response.first(),
                response.last()
        );
    }

    private RiskWordResponse toResponse(RiskWordItem item) {
        return new RiskWordResponse(
                item.id(),
                item.word(),
                item.description(),
                severityToText(item.severity()),
                item.active()
        );
    }

    private String severityToText(RiskSeverity severity) {
        return severity != null ? severity.name() : null;
    }
}
