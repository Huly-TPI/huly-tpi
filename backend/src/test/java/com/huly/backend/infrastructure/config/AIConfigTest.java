package com.huly.backend.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIConfigTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    private final AIConfig config = new AIConfig();

    @Test
    @DisplayName("Construye el ChatClient a partir del builder recibido")
    void chatClientShouldReturnClientBuiltFromBuilder() {
        // --- arrange ---
        givenBuilderReturnsClient();

        // --- act ---
        ChatClient result = createChatClient();

        // --- assert ---
        thenReturnsBuiltClient(result);
    }

    // --- arrange ---

    private void givenBuilderReturnsClient() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
    }

    // --- act ---

    private ChatClient createChatClient() {
        return config.chatClient(chatClientBuilder);
    }

    // --- assert ---

    private void thenReturnsBuiltClient(ChatClient result) {
        assertThat(result).isSameAs(chatClient);
    }
}
