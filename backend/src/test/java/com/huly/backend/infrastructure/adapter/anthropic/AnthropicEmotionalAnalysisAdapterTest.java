package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.EmotionalAnalysisResult;
import com.huly.backend.domain.model.enums.EmotionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnthropicEmotionalAnalysisAdapterTest {

    private ChatModel chatModel;
    private AnthropicEmotionalAnalysisAdapter adapter;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        adapter = new AnthropicEmotionalAnalysisAdapter(chatModel);
    }

    @Test
    void analyze_shouldParseValidStructuredJson() {
        givenModelReturns("""
                {
                  "shouldRecommend": true,
                  "detectedEmotion": "GRIEF",
                  "confidence": 0.92,
                  "valence": -0.85,
                  "arousal": 0.35,
                  "dominance": -0.75,
                  "intensity": 0.88,
                  "userGoal": "aliviar tristeza",
                  "shortReason": "duelo"
                }""");

        EmotionalAnalysisResult result = adapter.analyze("prompt", "mensaje", List.of());

        assertThat(result.shouldRecommend()).isTrue();
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.GRIEF);
        assertThat(result.confidence()).isEqualTo(0.92);
        assertThat(result.intensity()).isEqualTo(0.88);
        assertThat(result.userGoal()).isEqualTo("aliviar tristeza");
    }

    @Test
    void analyze_shouldMapSpanishEmotionAliases() {
        givenModelReturns("""
                {
                  "shouldRecommend": true,
                  "detectedEmotion": "TRISTEZA",
                  "confidence": 0.9,
                  "valence": -0.7,
                  "arousal": 0.2,
                  "dominance": -0.5,
                  "intensity": 0.8
                }""");

        EmotionalAnalysisResult result = adapter.analyze("prompt", "mensaje", List.of());

        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.SADNESS);
    }

    @Test
    void analyze_shouldFallbackToNeutral_whenJsonIsInvalid() {
        givenModelReturns("no es json");

        EmotionalAnalysisResult result = adapter.analyze("prompt", "mensaje", List.of());

        assertThat(result.shouldRecommend()).isFalse();
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.NEUTRAL);
        assertThat(result.confidence()).isZero();
    }

    private void givenModelReturns(String text) {
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(text);
    }
}
