package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.DeleteRiskWordRequest;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteRiskWordUseCase {

    private final RiskWordService riskWordService;

    public void execute(DeleteRiskWordRequest request) {
        riskWordService.delete(request.id());
    }
}
