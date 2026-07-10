package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.CreateRiskWordMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateRiskWordUseCase {

    private final RiskWordService riskWordService;
    private final CreateRiskWordMapper mapper;

    public CreateRiskWordResponse execute(CreateRiskWordRequest request) {
        RiskWord created = riskWordService.create(mapper.toModel(request));
        return mapper.toResponse(created);
    }
}
