package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.EmotionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnthropicEmotionalAnalysisAdapterTest {

    private ChatClient chatClient;
    private AnthropicEmotionalAnalysisAdapter adapter;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        adapter = new AnthropicEmotionalAnalysisAdapter(chatClient);
    }

    @Test
    void analyze_shouldReturnParsedResult() {
        EmotionalAnalysisResult mockResult = new EmotionalAnalysisResult(true, EmotionType.GRIEF, 0.92, -0.85, 0.35, -0.75, 0.88, "aliviar tristeza", "duelo");
        
        when(chatClient.prompt().system(anyString()).messages(anyList()).user(anyString()).call().entity(EmotionalAnalysisResult.class))
                .thenReturn(mockResult);

        EmotionalAnalysisResult result = adapter.analyze("prompt", "mensaje", List.of());

        assertThat(result).isSameAs(mockResult);
    }

    @Test
    void analyze_shouldFallbackToNeutral_whenExceptionOccurs() {
        when(chatClient.prompt().system(anyString()).messages(anyList()).user(anyString()).call().entity(EmotionalAnalysisResult.class))
                .thenThrow(new RuntimeException("Error parsing JSON"));

        EmotionalAnalysisResult result = adapter.analyze("prompt", "mensaje", List.of());

        assertThat(result.shouldRecommend()).isFalse();
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.NEUTRAL);
    }
}
