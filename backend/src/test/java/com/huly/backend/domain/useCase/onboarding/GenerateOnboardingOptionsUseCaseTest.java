package com.huly.backend.domain.useCase.onboarding;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.provider.LLMChatPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks private GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;

    @Test
    void execute_shouldReturnListOfOptions_whenLLMReturnsValidJson(){
        String json = "{\"options\": [\"Meditar\", \"Caminar\", \"Respirar\", \"Leer\"]}";
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of(json));
        List<String> result = generateOnboardingOptionsUseCase.execute(2, "Desestresarme");
        assertThat(result).containsExactly("Meditar", "Caminar", "Respirar", "Leer");
    }

    @Test 
    void execute_shouldReturnDefaultOptions_whenLLMReturnsInvalidJson(){
        String invalidJson = "invalid json";
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of(invalidJson));
        List<String> result = generateOnboardingOptionsUseCase.execute(2, "Desestresarme");
        assertThat(result).hasSize(4);
    }

    @Test
    void execute_shouldReturnDefaultOptions_whenOptionsArraysIsEmpty(){ 
        when(llmChatPort.chat(any(), any(), any())).thenReturn(ChatReply.of("{\"options\": []}"));
        List<String> result = generateOnboardingOptionsUseCase.execute(2, "Desestresarme");
        assertThat(result).isNotEmpty();
    }

    @Test
    void execute_step2_shouldIncludePreviousAnswerInPrompt() {
        String json = "{\"options\": [\"A\", \"B\", \"C\", \"D\"]}";
        when(llmChatPort.chat(any(), eq("Mi objetivo es: Desestresarme"), any())).thenReturn(ChatReply.of(json));

        List<String> result = generateOnboardingOptionsUseCase.execute(2, "Desestresarme");
        assertThat(result).containsExactly("A", "B", "C", "D");
    }

    @Test
    void execute_step3_shouldIncludePreviousAnswerInUserMessage(){ 
        String json = "{\"options\": [\"A\", \"B\", \"C\", \"D\"]}";
        when(llmChatPort.chat(any(), eq("Me interesa: Meditar"), any())).thenReturn(ChatReply.of(json));
        List<String> result = generateOnboardingOptionsUseCase.execute(3, "Meditar");
        assertThat(result).containsExactly("A", "B", "C", "D");
    }
    
}
