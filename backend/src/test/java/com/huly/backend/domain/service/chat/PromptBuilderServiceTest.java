package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatPersonalizationContext;
import com.huly.backend.domain.model.chat.ChatUserIntent;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.RiskSeverity;
import com.huly.backend.domain.model.riskWord.RiskWord;
import com.huly.backend.domain.model.vector.VectorMemory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderServiceTest {

    private final PromptBuilderService service = new PromptBuilderService();

    @Test
    @DisplayName("El prompt enriquecido empieza con el prompt base recibido")
    void buildEnrichedPromptShouldStartWithBasePrompt() {
        String result = buildEnriched("mi prompt base");

        thenStartsWith(result, "mi prompt base");
    }

    @Test
    @DisplayName("Usa base vacía cuando el prompt base es null")
    void buildEnrichedPromptShouldUseEmptyBaseWhenBasePromptIsNull() {
        String result = buildEnriched(null);

        thenStartsWith(result, "\n\n=== INSTRUCCIONES DE RESPUESTA ===");
    }

    @Test
    @DisplayName("Incluye las instrucciones de formato JSON")
    void buildEnrichedPromptShouldIncludeJsonFormatInstructions() {
        String result = buildEnriched("");

        thenContains(result,
                "INSTRUCCIONES DE RESPUESTA",
                "huly_reply",
                "detected_emotion",
                "intensity",
                "risk_detected",
                "matched_word");
    }

    @Test
    @DisplayName("Incluye todos los tipos de emoción en las instrucciones")
    void buildEnrichedPromptShouldIncludeAllEmotionTypesInInstructions() {
        String result = buildEnriched("");

        thenContainsAllEmotionTypes(result);
    }

    @Test
    @DisplayName("No incluye la sección de palabras de riesgo cuando la lista está vacía")
    void buildEnrichedPromptShouldNotIncludeRiskWordsSectionWhenListIsEmpty() {
        String result = buildEnrichedWithRiskWords(List.of());

        thenDoesNotContain(result, "PALABRAS Y FRASES DE RIESGO");
    }

    @Test
    @DisplayName("No incluye la sección de palabras de riesgo cuando la lista es null")
    void buildEnrichedPromptShouldNotIncludeRiskWordsSectionWhenListIsNull() {
        String result = buildEnrichedWithRiskWords(null);

        thenDoesNotContain(result, "PALABRAS Y FRASES DE RIESGO");
    }

    @Test
    @DisplayName("Incluye la sección de palabras de riesgo cuando la lista no está vacía")
    void buildEnrichedPromptShouldIncludeRiskWordsSectionWhenListIsNotEmpty() {
        String result = buildEnrichedWithRiskWords(List.of(riskWord("suicidio", RiskSeverity.HIGH, null)));

        thenContains(result,
                "PALABRAS Y FRASES DE RIESGO",
                "\"suicidio\"",
                "[HIGH]");
    }

    @Test
    @DisplayName("Incluye la descripción cuando la palabra de riesgo la tiene")
    void buildEnrichedPromptShouldIncludeDescriptionWhenRiskWordHasDescription() {
        String result = buildEnrichedWithRiskWords(
                List.of(riskWord("panico", RiskSeverity.MEDIUM, "descripción de prueba")));

        thenContains(result, "descripción de prueba");
    }

    @Test
    @DisplayName("No incluye descripción cuando la descripción es null")
    void buildEnrichedPromptShouldNotIncludeDescriptionWhenDescriptionIsNull() {
        String result = buildEnrichedWithRiskWords(
                List.of(riskWord("panico", RiskSeverity.MEDIUM, null)));

        thenDoesNotContain(result, " — ");
    }

    @Test
    @DisplayName("No incluye descripción cuando la descripción está en blanco")
    void buildEnrichedPromptShouldNotIncludeDescriptionWhenDescriptionIsBlank() {
        String result = buildEnrichedWithRiskWords(
                List.of(riskWord("panico", RiskSeverity.MEDIUM, "   ")));

        thenDoesNotContain(result, " — ");
    }

    @Test
    @DisplayName("Incluye todas las palabras de riesgo cuando hay varias")
    void buildEnrichedPromptShouldIncludeAllRiskWordsWhenMultipleProvided() {
        String result = buildEnrichedWithRiskWords(List.of(
                riskWord("suicidio", RiskSeverity.HIGH, null),
                riskWord("autolesion", RiskSeverity.MEDIUM, "daño físico")));

        thenContains(result,
                "\"suicidio\"", "[HIGH]",
                "\"autolesion\"", "[MEDIUM]", "daño físico");
    }

    @Test
    @DisplayName("El prompt de análisis emocional incluye instrucciones estructuradas y los recuerdos")
    void buildEmotionalAnalysisPromptShouldIncludeStructuredJsonInstructionsAndMemories() {
        String result = buildEmotionalAnalysis("base", List.of(memory("perdio a su perro")));

        thenContains(result,
                "ANALISIS EMOCIONAL ESTRUCTURADO",
                "perdio a su perro",
                "shouldRecommend",
                "detectedEmotion",
                "valence",
                "arousal",
                "dominance",
                "userGoal",
                "SADNESS");
    }

    @Test
    @DisplayName("Incluye las instrucciones de reto cuando el usuario lo solicitó")
    void buildEnrichedPromptShouldIncludeChallengeRequestInstructionsWhenUserAskedForChallenge() {
        String result = buildEnrichedWithIntent(ChatUserIntent.CHALLENGE_REQUEST);

        thenContains(result,
                "RETO SOLICITADO POR EL USUARIO",
                "Debes devolver generated_challenge",
                "Tambien debes presentar ese reto");
    }

    @Test
    @DisplayName("Incluye las preferencias conversacionales estructuradas")
    void buildEnrichedPromptShouldIncludeStructuredConversationPreferences() {
        String result = buildEnrichedWithPersonalization(
                personalization("Sergio Ramírez", "Checho", CommunicationStyle.DIRECT));

        thenContains(result,
                "PREFERENCIAS CONVERSACIONALES DEL USUARIO",
                "Nombre real registrado: Sergio Ramírez",
                "Nombre preferido: Checho",
                "Estilo preferido: directo",
                CommunicationStyle.DIRECT.promptInstruction(),
                "Respetá siempre el estilo preferido");
    }

    @Test
    @DisplayName("Omite el estilo cuando el estilo de comunicación es null")
    void buildEnrichedPromptShouldOmitStyleWhenCommunicationStyleIsNull() {
        String result = buildEnrichedWithPersonalization(
                personalization("Sergio", "Checho", null));

        thenContains(result,
                "PREFERENCIAS CONVERSACIONALES DEL USUARIO",
                "Nombre real registrado: Sergio",
                "Nombre preferido: Checho");
        thenDoesNotContain(result, "Estilo preferido:");
    }

    @Test
    @DisplayName("Omite los nombres cuando los valores de confianza son null o están en blanco")
    void buildEnrichedPromptShouldOmitNamesWhenTrustedValuesAreNullOrBlank() {
        String result = buildEnrichedWithPersonalization(
                personalization(null, "   ", CommunicationStyle.NEUTRAL));

        thenContains(result,
                "PREFERENCIAS CONVERSACIONALES DEL USUARIO",
                "Estilo preferido: neutro");
        thenDoesNotContain(result, "Nombre real registrado:");
        thenDoesNotContain(result, "Nombre preferido:");
    }

    @Test
    @DisplayName("Sanitiza los saltos de línea de los valores de confianza")
    void buildEnrichedPromptShouldSanitizeTrustedValueControlCharacters() {
        String result = buildEnrichedWithPersonalization(
                personalization("Line1\nLine2", null, CommunicationStyle.NEUTRAL));

        thenContains(result, "Nombre real registrado: Line1 Line2");
    }

    @Test
    @DisplayName("Incluye solo los recuerdos con contenido válido")
    void buildEnrichedPromptShouldIncludeMemoriesSectionWithValidContentOnly() {
        String result = buildEnrichedWithMemories(
                Arrays.asList(memory(null), memory("   "), memory("recuerdo valido")));

        thenContains(result,
                "INFORMACION RECORDADA DEL USUARIO",
                "- recuerdo valido");
    }

    @Test
    @DisplayName("Omite la sección de recuerdos cuando la lista es null")
    void buildEnrichedPromptShouldOmitMemoriesSectionWhenMemoriesAreNull() {
        String result = buildEnrichedWithMemories(null);

        thenDoesNotContain(result, "INFORMACION RECORDADA DEL USUARIO");
    }

    @Test
    @DisplayName("Incluye el contexto de la actividad recomendada por el sistema")
    void buildEnrichedPromptShouldIncludeSuggestedActionContext() {
        String result = buildEnrichedWithAction(
                action(ActivityType.BREATHING, "Respirar", "Hacé 5 respiraciones"));

        thenContains(result,
                "ACTIVIDAD RECOMENDADA POR EL SISTEMA",
                "Actividad: Respirar",
                "Tipo: BREATHING",
                "Descripción: Hacé 5 respiraciones");
    }

    @Test
    @DisplayName("Deja los campos de la actividad vacíos cuando sus valores son null")
    void buildEnrichedPromptShouldRenderEmptyFieldsWhenActionValuesAreNull() {
        String result = buildEnrichedWithAction(action(null, null, null));

        thenContains(result,
                "ACTIVIDAD RECOMENDADA POR EL SISTEMA",
                "\nActividad: \nTipo: \nDescripción: ");
    }

    // --- arrange ---
    private RiskWord riskWord(String word, RiskSeverity severity, String description) {
        return RiskWord.builder()
                .word(word)
                .severity(severity)
                .description(description)
                .active(true)
                .build();
    }

    private VectorMemory memory(String content) {
        return new VectorMemory("mem-1", 1L, null, null, content, null, 0.9);
    }

    private SuggestedChatAction action(ActivityType type, String title, String description) {
        return new SuggestedChatAction(type, 1L, title, description, null, null);
    }

    private ChatPersonalizationContext personalization(
            String registeredName,
            String preferredName,
            CommunicationStyle style) {
        return new ChatPersonalizationContext(registeredName, preferredName, style);
    }

    // --- act ---
    private String buildEnriched(String basePrompt) {
        return service.buildEnrichedPrompt(basePrompt, List.of(), List.of(), null, ChatUserIntent.NONE, null);
    }

    private String buildEnrichedWithRiskWords(List<RiskWord> riskWords) {
        return service.buildEnrichedPrompt("base", riskWords, List.of(), null, ChatUserIntent.NONE, null);
    }

    private String buildEnrichedWithMemories(List<VectorMemory> memories) {
        return service.buildEnrichedPrompt("base", List.of(), memories, null, ChatUserIntent.NONE, null);
    }

    private String buildEnrichedWithAction(SuggestedChatAction action) {
        return service.buildEnrichedPrompt("base", List.of(), List.of(), action, ChatUserIntent.NONE, null);
    }

    private String buildEnrichedWithIntent(ChatUserIntent intent) {
        return service.buildEnrichedPrompt("base", List.of(), List.of(), null, intent, null);
    }

    private String buildEnrichedWithPersonalization(ChatPersonalizationContext personalization) {
        return service.buildEnrichedPrompt("base", List.of(), List.of(), null, ChatUserIntent.NONE, personalization);
    }

    private String buildEmotionalAnalysis(String basePrompt, List<VectorMemory> memories) {
        return service.buildEmotionalAnalysisPrompt(basePrompt, memories);
    }

    // --- assert ---
    private void thenStartsWith(String result, String expected) {
        assertThat(result).startsWith(expected);
    }

    private void thenContains(String result, String... expected) {
        assertThat(result).contains(expected);
    }

    private void thenDoesNotContain(String result, String unexpected) {
        assertThat(result).doesNotContain(unexpected);
    }

    private void thenContainsAllEmotionTypes(String result) {
        String emotionList = Arrays.stream(EmotionType.values())
                .map(EmotionType::name)
                .collect(Collectors.joining("|"));
        assertThat(result).contains(emotionList);
    }
}
