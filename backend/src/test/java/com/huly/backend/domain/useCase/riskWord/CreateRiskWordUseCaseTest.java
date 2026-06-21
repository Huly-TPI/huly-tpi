package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private CreateRiskWordUseCase createRiskWordUseCase;

    @Test
    void execute_shouldBuildRiskWordWithActiveTrueAndDelegateToService() {
        RiskWord saved = RiskWord.builder().id(1L).word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        when(riskWordService.create(any(RiskWord.class))).thenReturn(saved);

        ArgumentCaptor<RiskWord> captor = ArgumentCaptor.forClass(RiskWord.class);
        createRiskWordUseCase.execute("suicidio", "desc", RiskSeverity.HIGH);

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

        RiskWord result = createRiskWordUseCase.execute("suicidio", null, RiskSeverity.HIGH);

        assertThat(result.getId()).isEqualTo(1L);
    }
}
