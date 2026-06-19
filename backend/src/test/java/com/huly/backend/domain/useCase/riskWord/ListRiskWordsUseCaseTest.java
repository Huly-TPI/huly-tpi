package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private ListRiskWordsUseCase listRiskWordsUseCase;

    @Test
    void execute_shouldDelegateToServiceAndReturnPage() {
        Page<RiskWord> page = new PageImpl<>(List.of());
        Pageable pageable = PageRequest.of(0, 20);
        when(riskWordService.list("suicidio", true, "HIGH", pageable)).thenReturn(page);

        Page<RiskWord> result = listRiskWordsUseCase.execute("suicidio", true, "HIGH", pageable);

        assertThat(result).isNotNull();
        verify(riskWordService).list("suicidio", true, "HIGH", pageable);
    }
}
