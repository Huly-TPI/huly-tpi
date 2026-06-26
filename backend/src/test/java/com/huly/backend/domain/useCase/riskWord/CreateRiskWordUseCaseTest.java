package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.CreateRiskWordMapper;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRiskWordUseCaseTest {

    @Mock
    private RiskWordService riskWordService;

    private CreateRiskWordUseCase createRiskWordUseCase;

    @BeforeEach
    void setUp() {
        createRiskWordUseCase = new CreateRiskWordUseCase(riskWordService, new CreateRiskWordMapper());
    }

    @Test
    void execute_shouldBuildRiskWordWithActiveTrueAndDelegateToService() {
        RiskWord saved = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(riskWordService.create(any(RiskWord.class))).thenReturn(saved);

        ArgumentCaptor<RiskWord> captor = ArgumentCaptor.forClass(RiskWord.class);
        createRiskWordUseCase.execute(new CreateRiskWordRequest("suicidio", "desc", RiskSeverity.HIGH));

        verify(riskWordService).create(captor.capture());
        RiskWord captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getWord()).isEqualTo("suicidio");
        assertThat(captured.getDescription()).isEqualTo("desc");
        assertThat(captured.getSeverity()).isEqualTo(RiskSeverity.HIGH);
        assertThat(captured.isActive()).isTrue();
    }

    @Test
    void execute_shouldReturnValueFromService() {
        RiskWord saved = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(riskWordService.create(any(RiskWord.class))).thenReturn(saved);

        CreateRiskWordResponse result = createRiskWordUseCase.execute(
                new CreateRiskWordRequest("suicidio", null, RiskSeverity.HIGH));

        assertThat(result.id()).isEqualTo(1L);
    }
}
