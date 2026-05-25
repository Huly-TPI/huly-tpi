package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.RiskSeverity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderServiceTest {

    private final PromptBuilderService service = new PromptBuilderService();

    @Test
    void buildEnrichedPrompt_shouldStartWithBasePrompt() {
        String result = service.buildEnrichedPrompt("mi prompt base", List.of());
        assertThat(result).startsWith("mi prompt base");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeJsonFormatInstructions() {
        String result = service.buildEnrichedPrompt("", List.of());
        assertThat(result)
                .contains("INSTRUCCIONES DE RESPUESTA")
                .contains("huly_reply")
                .contains("detected_emotion")
                .contains("intensity")
                .contains("risk_detected")
                .contains("matched_word");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeAllEmotionTypesInInstructions() {
        String result = service.buildEnrichedPrompt("", List.of());
        String emotionList = Arrays.stream(EmotionType.values())
                .map(EmotionType::name)
                .collect(Collectors.joining("|"));
        assertThat(result).contains(emotionList);
    }

    @Test
    void buildEnrichedPrompt_shouldNotIncludeRiskWordsSection_whenListIsEmpty() {
        String result = service.buildEnrichedPrompt("base", List.of());
        assertThat(result).doesNotContain("PALABRAS Y FRASES DE RIESGO");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeRiskWordsSection_whenListIsNotEmpty() {
        RiskWord rw = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        String result = service.buildEnrichedPrompt("base", List.of(rw));
        assertThat(result)
                .contains("PALABRAS Y FRASES DE RIESGO")
                .contains("\"suicidio\"")
                .contains("[HIGH]");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeDescription_whenRiskWordHasDescription() {
        RiskWord rw = RiskWord.builder().word("panico").severity(RiskSeverity.MEDIUM)
                .description("descripción de prueba").active(true).build();
        String result = service.buildEnrichedPrompt("base", List.of(rw));
        assertThat(result).contains("descripción de prueba");
    }

    @Test
    void buildEnrichedPrompt_shouldNotIncludeDescription_whenDescriptionIsNull() {
        RiskWord rw = RiskWord.builder().word("panico").severity(RiskSeverity.MEDIUM)
                .description(null).active(true).build();
        String result = service.buildEnrichedPrompt("base", List.of(rw));
        assertThat(result).doesNotContain(" — ");
    }

    @Test
    void buildEnrichedPrompt_shouldNotIncludeDescription_whenDescriptionIsBlank() {
        RiskWord rw = RiskWord.builder().word("panico").severity(RiskSeverity.MEDIUM)
                .description("   ").active(true).build();
        String result = service.buildEnrichedPrompt("base", List.of(rw));
        assertThat(result).doesNotContain(" — ");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeAllRiskWords_whenMultipleProvided() {
        RiskWord rw1 = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        RiskWord rw2 = RiskWord.builder().word("autolesion").severity(RiskSeverity.MEDIUM)
                .description("daño físico").active(true).build();
        String result = service.buildEnrichedPrompt("base", List.of(rw1, rw2));
        assertThat(result)
                .contains("\"suicidio\"").contains("[HIGH]")
                .contains("\"autolesion\"").contains("[MEDIUM]").contains("daño físico");
    }
}
