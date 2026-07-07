package com.huly.backend.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChatModelConfigTest {

    @Mock
    private AnthropicChatModel anthropicChatModel;

    @Mock
    private ChatModel chatModel;

    private final ChatModelConfig config = new ChatModelConfig();

    @Test
    @DisplayName("Expone el AnthropicChatModel como el ChatModel primario")
    void anthropicPrimaryModelShouldReturnSameModelInstance() {
        // --- act ---
        ChatModel result = createPrimaryModel();

        // --- assert ---
        thenReturnsSameModel(result);
    }

    @Test
    @DisplayName("Crea un ChatClient no nulo a partir del ChatModel")
    void anthropicChatClientShouldReturnNonNullClient() {
        // --- act ---
        ChatClient result = createChatClient();

        // --- assert ---
        thenClientIsNotNull(result);
    }

    // --- act ---

    private ChatModel createPrimaryModel() {
        return config.anthropicPrimaryModel(anthropicChatModel);
    }

    private ChatClient createChatClient() {
        return config.anthropicChatClient(chatModel);
    }

    // --- assert ---

    private void thenReturnsSameModel(ChatModel result) {
        assertThat(result).isSameAs(anthropicChatModel);
    }

    private void thenClientIsNotNull(ChatClient result) {
        assertThat(result).isNotNull();
    }
}
