package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.mapper.riskWord.ListRiskWordsMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListRiskWordsUseCase {

    private final RiskWordService riskWordService;
    private final ListRiskWordsMapper mapper;

    public ListRiskWordsResponse execute(ListRiskWordsRequest request) {
        Page<RiskWord> result = riskWordService.list(
                request.word(), request.active(), request.severity(), mapper.toPageable(request));
        return mapper.toResponse(result);
    }
}
