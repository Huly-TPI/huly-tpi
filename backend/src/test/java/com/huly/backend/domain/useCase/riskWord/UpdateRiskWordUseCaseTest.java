package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.UpdateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.UpdateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.UpdateRiskWordMapper;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.service.chat.RiskWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRiskWordUseCaseTest {

    private static final Long RISK_WORD_ID = 1L;
    private static final String WORD = "panico";
    private static final String DESCRIPTION = "desc";
    private static final RiskSeverity SEVERITY = RiskSeverity.MEDIUM;

    @Mock
    private RiskWordService riskWordService;

    private UpdateRiskWordUseCase updateRiskWordUseCase;

    @BeforeEach
    void setUp() {
        updateRiskWordUseCase = new UpdateRiskWordUseCase(riskWordService, new UpdateRiskWordMapper());
    }

    @Test
    @DisplayName("Construye la palabra con los nuevos datos y la delega al servicio con su id")
    void executeShouldBuildRiskWordAndDelegateToServiceWithId() {
        givenServiceUpdatesRiskWord();

        update();

        thenServiceReceivedUpdatedRiskWordForId();
    }

    @Test
    @DisplayName("Devuelve la respuesta mapeada desde la palabra actualizada por el servicio")
    void executeShouldReturnResponseMappedFromUpdatedRiskWord() {
        givenServiceUpdatesRiskWord();

        UpdateRiskWordResponse result = update();

        thenResponseMatchesUpdatedRiskWord(result);
    }

    // --- arrange ---

    private void givenServiceUpdatesRiskWord() {
        when(riskWordService.update(eq(RISK_WORD_ID), any(RiskWord.class))).thenReturn(updatedRiskWord());
    }

    private UpdateRiskWordRequest updateRequest() {
        return new UpdateRiskWordRequest(RISK_WORD_ID, WORD, DESCRIPTION, SEVERITY);
    }

    private RiskWord updatedRiskWord() {
        return RiskWord.builder()
                .id(RISK_WORD_ID)
                .word(WORD)
                .description(DESCRIPTION)
                .severity(SEVERITY)
                .active(true)
                .build();
    }

    // --- act ---

    private UpdateRiskWordResponse update() {
        return updateRiskWordUseCase.execute(updateRequest());
    }

    // --- assert ---

    private void thenServiceReceivedUpdatedRiskWordForId() {
        ArgumentCaptor<RiskWord> captor = ArgumentCaptor.forClass(RiskWord.class);
        verify(riskWordService).update(eq(RISK_WORD_ID), captor.capture());
        RiskWord captured = captor.getValue();
        assertThat(captured.getWord()).isEqualTo(WORD);
        assertThat(captured.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(captured.getSeverity()).isEqualTo(SEVERITY);
    }

    private void thenResponseMatchesUpdatedRiskWord(UpdateRiskWordResponse result) {
        assertThat(result.id()).isEqualTo(RISK_WORD_ID);
        assertThat(result.word()).isEqualTo(WORD);
        assertThat(result.description()).isEqualTo(DESCRIPTION);
        assertThat(result.severity()).isEqualTo(SEVERITY);
        assertThat(result.active()).isTrue();
    }
}
