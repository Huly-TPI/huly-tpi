package com.huly.backend.domain.useCase.onboarding;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsRequest;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsResponse;
import com.huly.backend.domain.mapper.onboarding.GenerateOnboardingOptionsMapper;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.port.LLMChatPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateOnboardingOptionsUseCaseTest {

    @Mock private LLMChatPort llmChatPort;

    private GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;

    @BeforeEach
    void setUp() {
        generateOnboardingOptionsUseCase =
                new GenerateOnboardingOptionsUseCase(llmChatPort, new GenerateOnboardingOptionsMapper());
    }

    @Test
    void execute_shouldReturnListOfOptions_whenLLMReturnsValidJson(){
        String json = "{\"options\": [\"Meditar\", \"Caminar\", \"Respirar\", \"Leer\"]}";
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of(json));
        GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
        assertThat(result.options()).containsExactly("Meditar", "Caminar", "Respirar", "Leer");
    }

    @Test
    void execute_shouldReturnDefaultOptions_whenLLMReturnsInvalidJson(){
        String invalidJson = "invalid json";
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of(invalidJson));
        GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
        assertThat(result.options()).hasSize(4);
    }

    @Test
    void execute_shouldReturnDefaultOptions_whenOptionsArraysIsEmpty(){
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("{\"options\": []}"));
        GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
        assertThat(result.options()).isNotEmpty();
    }

  @Test
        void execute_step2_shouldIncludePreviousAnswerInPrompt() {
            String json = "{\"options\": [\"A\", \"B\", \"C\", \"D\"]}";
            when(llmChatPort.chat(any(), eq("Mi objetivo emocional es: \"Desestresarme\""), any())).thenReturn(ChatReply.of(json));

            GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
            assertThat(result.options()).containsExactly("A", "B", "C", "D");
        }

    @Test
        void execute_step3_shouldIncludePreviousAnswerInUserMessage(){
            String json = "{\"options\": [\"A\", \"B\", \"C\", \"D\"]}";
            when(llmChatPort.chat(any(), eq("El aspecto que más me llama es: \"Meditar\""), any())).thenReturn(ChatReply.of(json));
            GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(3, "Meditar"));
            assertThat(result.options()).containsExactly("A", "B", "C", "D");
        }

    @Test
        void execute_shouldReturnDefaultOptions_whenLlmReturnsJsonWithBracesInWrongOrder() {
            when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("} texto inválido {"));
            GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
            assertThat(result.options()).hasSize(4);
        }

    @Test
        void execute_shouldReturnDefaultOptions_whenOptionsNodeIsNotArray() {
            when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("{\"options\": \"no es un array\"}"));
            GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
            assertThat(result.options()).hasSize(4);
        }

    @Test
    void execute_shouldReturnDefaultOptions_whenJsonParsingThrows() {
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("{broken json}"));
        GenerateOnboardingOptionsResponse result = generateOnboardingOptionsUseCase.execute(new GenerateOnboardingOptionsRequest(2, "Desestresarme"));
        assertThat(result.options()).hasSize(4);
    }
}
