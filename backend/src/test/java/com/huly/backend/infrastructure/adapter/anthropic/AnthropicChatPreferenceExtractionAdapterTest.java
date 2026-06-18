package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnthropicChatPreferenceExtractionAdapterTest {

    private ChatClient chatClient;
    private AnthropicChatPreferenceExtractionAdapter adapter;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        adapter = new AnthropicChatPreferenceExtractionAdapter(new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()), chatClient);
    }

    @Test
    void extract_shouldReturnValidResult() {
        ChatPreferenceDetectionResult mockResult = new ChatPreferenceDetectionResult(
                "Juan",
                null,
                ChatPreferenceMessageType.PREFERENCE_ONLY,
                0.9
        );
        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(ChatPreferenceDetectionResult.class))
                .thenReturn(mockResult);

        ChatPreferenceDetectionResult result = adapter.extract("Llámame Juan", ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result).isSameAs(mockResult);
    }

    @Test
    void extract_shouldReturnUnrelated_onException() {
        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(ChatPreferenceDetectionResult.class))
                .thenThrow(new RuntimeException("Error"));

        ChatPreferenceDetectionResult result = adapter.extract("hola", ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.UNRELATED);
    }
}
