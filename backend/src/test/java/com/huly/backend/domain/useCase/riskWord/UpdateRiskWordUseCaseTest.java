package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.service.RiskWordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRiskWordUseCaseTest {

    @Mock
    private RiskWordService riskWordService;

    @InjectMocks
    private UpdateRiskWordUseCase updateRiskWordUseCase;

    @Test
    void execute_shouldBuildRiskWordAndDelegateToService() {
        RiskWord updated = RiskWord.builder().id(1L).word("panico").severity(RiskSeverity.MEDIUM).active(true).build();
        when(riskWordService.update(eq(1L), any(RiskWord.class))).thenReturn(updated);

        ArgumentCaptor<RiskWord> captor = ArgumentCaptor.forClass(RiskWord.class);
        updateRiskWordUseCase.execute(1L, "panico", "desc", RiskSeverity.MEDIUM);

        verify(riskWordService).update(eq(1L), captor.capture());
        RiskWord captured = captor.getValue();
        assertThat(captured.getWord()).isEqualTo("panico");
        assertThat(captured.getDescription()).isEqualTo("desc");
        assertThat(captured.getSeverity()).isEqualTo(RiskSeverity.MEDIUM);
    }

    @Test
    void execute_shouldReturnValueFromService() {
        RiskWord updated = RiskWord.builder().id(1L).word("panico").severity(RiskSeverity.MEDIUM).active(true).build();
        when(riskWordService.update(eq(1L), any(RiskWord.class))).thenReturn(updated);

        RiskWord result = updateRiskWordUseCase.execute(1L, "panico", null, RiskSeverity.MEDIUM);

        assertThat(result.getWord()).isEqualTo("panico");
    }
}
