package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.ListRiskWordsRequest;
import com.huly.backend.domain.dto.riskWord.ListRiskWordsResponse;
import com.huly.backend.domain.mapper.riskWord.ListRiskWordsMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRiskWordsUseCaseTest {

    @Mock
    private RiskWordService riskWordService;

    private ListRiskWordsUseCase listRiskWordsUseCase;

    @BeforeEach
    void setUp() {
        listRiskWordsUseCase = new ListRiskWordsUseCase(riskWordService, new ListRiskWordsMapper());
    }

    @Test
    void execute_shouldDelegateToServiceAndReturnPage() {
        Page<RiskWord> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);
        when(riskWordService.list("suicidio", true, "HIGH", pageable)).thenReturn(page);

        ListRiskWordsResponse result = listRiskWordsUseCase.execute(
                new ListRiskWordsRequest("suicidio", true, "HIGH", 0, 20));

        assertThat(result).isNotNull();
        verify(riskWordService).list("suicidio", true, "HIGH", pageable);
    }
}
