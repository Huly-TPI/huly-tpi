package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.UpdateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.UpdateRiskWordMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateRiskWordUseCase {

    private final RiskWordService riskWordService;
    private final UpdateRiskWordMapper mapper;

    public UpdateRiskWordResponse execute(UpdateRiskWordRequest request) {
        RiskWord updated = riskWordService.update(request.id(), mapper.toModel(request));
        return mapper.toResponse(updated);
    }
}
