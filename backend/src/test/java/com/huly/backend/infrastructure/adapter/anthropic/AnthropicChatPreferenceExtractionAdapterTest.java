package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicChatPreferenceExtractionAdapterTest {

    private FakeChatModel chatModel;
    private AnthropicChatPreferenceExtractionAdapter adapter;

    @BeforeEach
    void setUp() {
        chatModel = new FakeChatModel();
        adapter = new AnthropicChatPreferenceExtractionAdapter(chatModel);
    }

    @Test
    void extract_shouldParseStructuredPreference() {
        givenModelReturns("""
                {
                  "preferredName": "Crack",
                  "communicationStyle": "DIRECT",
                  "messageType": "PREFERENCE_ONLY",
                  "confidence": 0.97
                }
                """);

        ChatPreferenceDetectionResult result = adapter.extract(
                "Me llamo Sergio pero me podés decir crack y hablame directo",
                ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.preferredName()).isEqualTo("Crack");
        assertThat(result.communicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.PREFERENCE_ONLY);
        assertThat(result.confidence()).isEqualTo(0.97);
    }

    @Test
    void extract_shouldParseJsonSurroundedByText() {
        givenModelReturns("""
                Resultado:
                {"preferredName":null,"communicationStyle":"FORMAL","messageType":"MIXED","confidence":0.91}
                Fin.
                """);

        ChatPreferenceDetectionResult result = adapter.extract(
                "Hablame formal, además necesito ayuda",
                ChatPreferenceExpectedField.COMMUNICATION_STYLE);

        assertThat(result.preferredName()).isNull();
        assertThat(result.communicationStyle()).isEqualTo(CommunicationStyle.FORMAL);
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.MIXED);
        assertThat(result.confidence()).isEqualTo(0.91);
    }

    @Test
    void extract_shouldReturnUnrelatedWhenResponseIsInvalid() {
        givenModelReturns("respuesta sin JSON");

        ChatPreferenceDetectionResult result = adapter.extract(
                "Hola",
                ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.hasPreference()).isFalse();
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.UNRELATED);
        assertThat(result.confidence()).isZero();
    }

    @Test
    void extract_shouldReturnUnrelatedWhenModelFails() {
        chatModel.failWith(new IllegalStateException("model unavailable"));

        ChatPreferenceDetectionResult result = adapter.extract(
                "Decime Crack",
                ChatPreferenceExpectedField.PREFERRED_NAME);

        assertThat(result.hasPreference()).isFalse();
        assertThat(result.messageType()).isEqualTo(ChatPreferenceMessageType.UNRELATED);
    }

    private void givenModelReturns(String text) {
        chatModel.respondWith(text);
    }

    private static final class FakeChatModel implements ChatModel {

        private String response;
        private RuntimeException failure;

        @Override
        public ChatResponse call(Prompt prompt) {
            if (failure != null) {
                throw failure;
            }
            return new ChatResponse(List.of(
                    new Generation(new AssistantMessage(response))));
        }

        private void respondWith(String text) {
            response = text;
            failure = null;
        }

        private void failWith(RuntimeException exception) {
            failure = exception;
        }
    }
}
