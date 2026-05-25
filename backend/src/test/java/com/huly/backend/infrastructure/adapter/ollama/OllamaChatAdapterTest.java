package com.huly.backend.infrastructure.adapter.ollama;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OllamaChatAdapterTest {

    private ChatModel chatModel;
    private OllamaChatAdapter adapter;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        adapter = new OllamaChatAdapter(chatModel);
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

    @Test
    void chat_shouldReturnReplyFromModelOutput() {
        givenModelReturns("respuesta del modelo");

        ChatReply result = adapter.chat("prompt del sistema", "hola", List.of());

        assertThat(result.content()).isEqualTo("respuesta del modelo");
        assertThat(result.detectedEmotion()).isNull();
        assertThat(result.riskDetected()).isNull();
    }

    @Test
    void chat_shouldIncludeSystemMessage_whenSystemPromptIsNotBlank() {
        givenModelReturns("resp");

        ChatReply result = adapter.chat("sos un asistente", "hola", List.of());

        assertThat(result.content()).isEqualTo("resp");
    }

    @Test
    void chat_shouldOmitSystemMessage_whenSystemPromptIsBlank() {
        givenModelReturns("resp");

        ChatReply result = adapter.chat("", "hola", List.of());

        assertThat(result.content()).isEqualTo("resp");
    }

    @Test
    void chat_shouldOmitSystemMessage_whenSystemPromptIsNull() {
        givenModelReturns("resp");

        ChatReply result = adapter.chat(null, "hola", List.of());

        assertThat(result.content()).isEqualTo("resp");
    }

    @Test
    void chat_shouldIncludeUserHistoryMessages() {
        givenModelReturns("resp");

        List<ConversationMessage> history = List.of(
                new ConversationMessage(MessageRole.USER, "mensaje anterior", null, null, null)
        );

        ChatReply result = adapter.chat("prompt", "nuevo", history);

        assertThat(result.content()).isEqualTo("resp");
    }

    @Test
    void chat_shouldIncludeAssistantHistoryMessages() {
        givenModelReturns("resp");

        List<ConversationMessage> history = List.of(
                new ConversationMessage(MessageRole.ASSISTANT, "respuesta anterior", null, null, null)
        );

        ChatReply result = adapter.chat("prompt", "nuevo", history);

        assertThat(result.content()).isEqualTo("resp");
    }

    @Test
    void chat_shouldHandleMixedHistory() {
        givenModelReturns("resp final");

        List<ConversationMessage> history = List.of(
                new ConversationMessage(MessageRole.USER, "pregunta", null, null, null),
                new ConversationMessage(MessageRole.ASSISTANT, "respuesta", null, null, null)
        );

        ChatReply result = adapter.chat("prompt", "siguiente", history);

        assertThat(result.content()).isEqualTo("resp final");
    }
}
