package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
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

class AnthropicChatAdapterTest {

    private ChatClient chatClient;
    private AnthropicChatAdapter adapter;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        adapter = new AnthropicChatAdapter(chatClient);
    }

    @Test
    void chat_shouldReturnParsedResult_whenLlmCallSucceeds() {
        AnthropicChatAdapter.ChatReplyDto mockDto = new AnthropicChatAdapter.ChatReplyDto(
                "hola", "JOY", 8, false, null, null
        );

        when(chatClient.prompt().system(anyString()).messages(anyList()).user(anyString()).call().entity(AnthropicChatAdapter.ChatReplyDto.class))
                .thenReturn(mockDto);

        ChatReply result = adapter.chat("sys", "user", List.of());

        assertThat(result.content()).isEqualTo("hola");
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.JOY);
        assertThat(result.intensity()).isEqualTo(8);
        assertThat(result.riskDetected()).isFalse();
    }

    @Test
    void chat_shouldReturnFallback_whenExceptionOccurs() {
        when(chatClient.prompt().system(anyString()).messages(anyList()).user(anyString()).call().entity(any(Class.class)))
                .thenThrow(new RuntimeException("Test Exception"));

        ChatReply result = adapter.chat("sys", "user", List.of());
        assertThat(result.content()).contains("Disculpa, estoy teniendo problemas");
    }
}
