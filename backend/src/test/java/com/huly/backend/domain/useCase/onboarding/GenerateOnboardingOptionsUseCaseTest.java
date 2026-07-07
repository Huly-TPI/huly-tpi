package com.huly.backend.domain.useCase.onboarding;

import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsRequest;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsResponse;
import com.huly.backend.domain.mapper.onboarding.GenerateOnboardingOptionsMapper;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.port.LLMChatPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateOnboardingOptionsUseCaseTest {

    private static final String JSON_ABCD = "{\"options\": [\"A\", \"B\", \"C\", \"D\"]}";

    @Mock
    private LLMChatPort llmChatPort;

    private GenerateOnboardingOptionsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateOnboardingOptionsUseCase(llmChatPort, new GenerateOnboardingOptionsMapper());
    }

    @Test
    @DisplayName("Devuelve la lista de opciones cuando el LLM responde un JSON válido")
    void executeReturnsListOfOptionsWhenLlmReturnsValidJson() {
        givenLlmReplies("{\"options\": [\"Meditar\", \"Caminar\", \"Respirar\", \"Leer\"]}");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenOptionsAre(result, "Meditar", "Caminar", "Respirar", "Leer");
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando el LLM responde un JSON inválido")
    void executeReturnsDefaultOptionsWhenLlmReturnsInvalidJson() {
        givenLlmReplies("invalid json");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenHasDefaultOptions(result);
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando el arreglo de opciones está vacío")
    void executeReturnsDefaultOptionsWhenOptionsArrayIsEmpty() {
        givenLlmReplies("{\"options\": []}");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenHasSomeOptions(result);
    }

    @Test
    @DisplayName("Incluye la respuesta anterior en el mensaje del paso 2")
    void executeStep2IncludesPreviousAnswerInUserMessage() {
        givenLlmRepliesForGoal("Desestresarme", JSON_ABCD);

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenOptionsAre(result, "A", "B", "C", "D");
    }

    @Test
    @DisplayName("Incluye la respuesta anterior en el mensaje del paso 3")
    void executeStep3IncludesPreviousAnswerInUserMessage() {
        givenLlmRepliesForAspect("Meditar", JSON_ABCD);

        GenerateOnboardingOptionsResponse result = generate(3, "Meditar");

        thenOptionsAre(result, "A", "B", "C", "D");
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando las llaves del JSON vienen en orden inverso")
    void executeReturnsDefaultOptionsWhenBracesInWrongOrder() {
        givenLlmReplies("} texto inválido {");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenHasDefaultOptions(result);
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando falta la llave de cierre del JSON")
    void executeReturnsDefaultOptionsWhenClosingBraceIsMissing() {
        givenLlmReplies("texto { sin cierre");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenHasDefaultOptions(result);
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando el nodo options no es un arreglo")
    void executeReturnsDefaultOptionsWhenOptionsNodeIsNotArray() {
        givenLlmReplies("{\"options\": \"no es un array\"}");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenHasDefaultOptions(result);
    }

    @Test
    @DisplayName("Devuelve las opciones por defecto cuando el parseo del JSON lanza excepción")
    void executeReturnsDefaultOptionsWhenJsonParsingThrows() {
        givenLlmReplies("{broken json}");

        GenerateOnboardingOptionsResponse result = generate(2, "Desestresarme");

        thenHasDefaultOptions(result);
    }

    // --- arrange ---

    private void givenLlmReplies(String content) {
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of(content));
    }

    private void givenLlmRepliesForGoal(String goal, String content) {
        when(llmChatPort.chat(any(), eq("Mi objetivo emocional es: \"" + goal + "\""), any()))
                .thenReturn(ChatReply.of(content));
    }

    private void givenLlmRepliesForAspect(String aspect, String content) {
        when(llmChatPort.chat(any(), eq("El aspecto que más me llama es: \"" + aspect + "\""), any()))
                .thenReturn(ChatReply.of(content));
    }

    // --- act ---

    private GenerateOnboardingOptionsResponse generate(int step, String previousAnswer) {
        return useCase.execute(new GenerateOnboardingOptionsRequest(step, previousAnswer));
    }

    // --- assert ---

    private void thenOptionsAre(GenerateOnboardingOptionsResponse result, String... expected) {
        assertThat(result.options()).containsExactly(expected);
    }

    private void thenHasDefaultOptions(GenerateOnboardingOptionsResponse result) {
        assertThat(result.options()).hasSize(4);
    }

    private void thenHasSomeOptions(GenerateOnboardingOptionsResponse result) {
        assertThat(result.options()).isNotEmpty();
    }
}
