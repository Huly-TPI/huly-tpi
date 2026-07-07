package com.huly.backend.domain.useCase.riskWord;

import com.huly.backend.domain.dto.riskWord.CreateRiskWordRequest;
import com.huly.backend.domain.dto.riskWord.CreateRiskWordResponse;
import com.huly.backend.domain.mapper.riskWord.CreateRiskWordMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRiskWordUseCaseTest {

    private static final Long GENERATED_ID = 1L;
    private static final String WORD = "suicidio";
    private static final String DESCRIPTION = "intención autolítica";
    private static final RiskSeverity SEVERITY = RiskSeverity.HIGH;

    @Mock
    private RiskWordService riskWordService;

    private CreateRiskWordUseCase createRiskWordUseCase;

    @BeforeEach
    void setUp() {
        createRiskWordUseCase = new CreateRiskWordUseCase(riskWordService, new CreateRiskWordMapper());
    }

    @Test
    @DisplayName("Construye una palabra de riesgo activa y sin id, y la delega al servicio")
    void executeShouldBuildActiveRiskWordWithoutIdAndDelegateToService() {
        givenServiceCreatesRiskWord();

        create();

        thenServiceReceivedNewActiveRiskWord();
    }

    @Test
    @DisplayName("Devuelve la respuesta mapeada desde la palabra creada por el servicio")
    void executeShouldReturnResponseMappedFromCreatedRiskWord() {
        givenServiceCreatesRiskWord();

        CreateRiskWordResponse result = create();

        thenResponseMatchesCreatedRiskWord(result);
    }

    // --- arrange ---

    private void givenServiceCreatesRiskWord() {
        when(riskWordService.create(any(RiskWord.class))).thenReturn(savedRiskWord());
    }

    private CreateRiskWordRequest createRequest() {
        return new CreateRiskWordRequest(WORD, DESCRIPTION, SEVERITY);
    }

    private RiskWord savedRiskWord() {
        return RiskWord.builder()
                .id(GENERATED_ID)
                .word(WORD)
                .description(DESCRIPTION)
                .severity(SEVERITY)
                .active(true)
                .build();
    }

    // --- act ---

    private CreateRiskWordResponse create() {
        return createRiskWordUseCase.execute(createRequest());
    }

    // --- assert ---

    private void thenServiceReceivedNewActiveRiskWord() {
        ArgumentCaptor<RiskWord> captor = ArgumentCaptor.forClass(RiskWord.class);
        verify(riskWordService).create(captor.capture());
        RiskWord captured = captor.getValue();
        assertThat(captured.getId()).isNull();
        assertThat(captured.getWord()).isEqualTo(WORD);
        assertThat(captured.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(captured.getSeverity()).isEqualTo(SEVERITY);
        assertThat(captured.isActive()).isTrue();
    }

    private void thenResponseMatchesCreatedRiskWord(CreateRiskWordResponse result) {
        assertThat(result.id()).isEqualTo(GENERATED_ID);
        assertThat(result.word()).isEqualTo(WORD);
        assertThat(result.description()).isEqualTo(DESCRIPTION);
        assertThat(result.severity()).isEqualTo(SEVERITY);
        assertThat(result.active()).isTrue();
    }
}
