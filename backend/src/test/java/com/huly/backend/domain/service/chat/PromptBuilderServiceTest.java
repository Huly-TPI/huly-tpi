package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatPersonalizationContext;
import com.huly.backend.domain.model.chat.ChatUserIntent;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.vector.VectorMemory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderServiceTest {

    private final PromptBuilderService service = new PromptBuilderService();

    @Test
    void buildEnrichedPrompt_shouldStartWithBasePrompt() {
        String result = buildPrompt("mi prompt base", List.of());
        assertThat(result).startsWith("mi prompt base");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeJsonFormatInstructions() {
        String result = buildPrompt("", List.of());
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
        String result = buildPrompt("", List.of());
        String emotionList = Arrays.stream(EmotionType.values())
                .map(EmotionType::name)
                .collect(Collectors.joining("|"));
        assertThat(result).contains(emotionList);
    }

    @Test
    void buildEnrichedPrompt_shouldNotIncludeRiskWordsSection_whenListIsEmpty() {
        String result = buildPrompt("base", List.of());
        assertThat(result).doesNotContain("PALABRAS Y FRASES DE RIESGO");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeRiskWordsSection_whenListIsNotEmpty() {
        RiskWord rw = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        String result = buildPrompt("base", List.of(rw));
        assertThat(result)
                .contains("PALABRAS Y FRASES DE RIESGO")
                .contains("\"suicidio\"")
                .contains("[HIGH]");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeDescription_whenRiskWordHasDescription() {
        RiskWord rw = RiskWord.builder().word("panico").severity(RiskSeverity.MEDIUM)
                .description("descripción de prueba").active(true).build();
        String result = buildPrompt("base", List.of(rw));
        assertThat(result).contains("descripción de prueba");
    }

    @Test
    void buildEnrichedPrompt_shouldNotIncludeDescription_whenDescriptionIsNull() {
        RiskWord rw = RiskWord.builder().word("panico").severity(RiskSeverity.MEDIUM)
                .description(null).active(true).build();
        String result = buildPrompt("base", List.of(rw));
        assertThat(result).doesNotContain(" — ");
    }

    @Test
    void buildEnrichedPrompt_shouldNotIncludeDescription_whenDescriptionIsBlank() {
        RiskWord rw = RiskWord.builder().word("panico").severity(RiskSeverity.MEDIUM)
                .description("   ").active(true).build();
        String result = buildPrompt("base", List.of(rw));
        assertThat(result).doesNotContain(" — ");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeAllRiskWords_whenMultipleProvided() {
        RiskWord rw1 = RiskWord.builder().word("suicidio").severity(RiskSeverity.HIGH).active(true).build();
        RiskWord rw2 = RiskWord.builder().word("autolesion").severity(RiskSeverity.MEDIUM)
                .description("daño físico").active(true).build();
        String result = buildPrompt("base", List.of(rw1, rw2));
        assertThat(result)
                .contains("\"suicidio\"").contains("[HIGH]")
                .contains("\"autolesion\"").contains("[MEDIUM]").contains("daño físico");
    }

    @Test
    void buildEmotionalAnalysisPrompt_shouldIncludeStructuredJsonInstructionsAndMemories() {
        VectorMemory memory = new VectorMemory("mem-1", 1L, null, null, "perdio a su perro", null, 0.9);

        String result = service.buildEmotionalAnalysisPrompt("base", List.of(memory));

        assertThat(result)
                .contains("ANALISIS EMOCIONAL ESTRUCTURADO")
                .contains("perdio a su perro")
                .contains("shouldRecommend")
                .contains("detectedEmotion")
                .contains("valence")
                .contains("arousal")
                .contains("dominance")
                .contains("userGoal")
                .contains("SADNESS");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeChallengeRequestInstructions_whenUserAskedForChallenge() {
        String result = service.buildEnrichedPrompt(
                "base",
                List.of(),
                List.of(),
                null,
                ChatUserIntent.CHALLENGE_REQUEST,
                null
        );

        assertThat(result)
                .contains("RETO SOLICITADO POR EL USUARIO")
                .contains("Debes devolver generated_challenge")
                .contains("Tambien debes presentar ese reto");
    }

    @Test
    void buildEnrichedPrompt_shouldIncludeStructuredConversationPreferences() {
        ChatPersonalizationContext personalization = new ChatPersonalizationContext(
                "Sergio Ramírez",
                "Checho",
                CommunicationStyle.DIRECT);

        String result = service.buildEnrichedPrompt(
                "base",
                List.of(),
                List.of(),
                null,
                ChatUserIntent.NONE,
                personalization);

        assertThat(result)
                .contains("PREFERENCIAS CONVERSACIONALES DEL USUARIO")
                .contains("Nombre real registrado: Sergio Ramírez")
                .contains("Nombre preferido: Checho")
                .contains("Estilo preferido: directo")
                .contains(CommunicationStyle.DIRECT.promptInstruction())
                .contains("Respetá siempre el estilo preferido");
    }

    private String buildPrompt(String basePrompt, List<RiskWord> riskWords) {
        return service.buildEnrichedPrompt(
                basePrompt,
                riskWords,
                List.of(),
                null,
                ChatUserIntent.NONE,
                null);
    }
}
