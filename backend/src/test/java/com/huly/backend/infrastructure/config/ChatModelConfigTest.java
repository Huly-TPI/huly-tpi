package com.huly.backend.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ChatModelConfigTest {

    private final ChatModelConfig config = new ChatModelConfig();

    @Test
    void anthropicPrimaryModel_shouldReturnSameModelInstance() {
        AnthropicChatModel model = mock(AnthropicChatModel.class);

        ChatModel result = config.anthropicPrimaryModel(model);

        assertThat(result).isSameAs(model);
    }

    @Test
    void ollamaPrimaryModel_shouldReturnSameModelInstance() {
        OllamaChatModel model = mock(OllamaChatModel.class);

        ChatModel result = config.ollamaPrimaryModel(model);

        assertThat(result).isSameAs(model);
    }
}
