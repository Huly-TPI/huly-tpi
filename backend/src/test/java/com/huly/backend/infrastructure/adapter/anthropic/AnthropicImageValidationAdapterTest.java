package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import com.huly.backend.domain.model.ImageValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnthropicImageValidationAdapterTest {

    private ChatClient chatClient;
    private AnthropicImageValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        adapter = new AnthropicImageValidationAdapter(chatClient);
    }

    @Test
    void validate_shouldReturnResult() {
        ImageValidationResult mockResult = new ImageValidationResult(true, "¡Excelente!");
        
        when(chatClient.prompt().messages(any(org.springframework.ai.chat.messages.Message.class)).call().entity(ImageValidationResult.class))
                .thenReturn(mockResult);

        ImageValidationResult result = adapter.validate(new byte[]{1, 2, 3}, "image/png", "Reto", "Desc");

        assertThat(result).isSameAs(mockResult);
    }

    @Test
    void validate_shouldThrowException_whenApiFails() {
        when(chatClient.prompt().messages(any(org.springframework.ai.chat.messages.Message.class)).call().entity(ImageValidationResult.class))
                .thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> adapter.validate(new byte[]{1, 2, 3}, "image/png", "Reto", "Desc"))
                .isInstanceOf(ImageValidationUnavailableException.class);
    }
}
