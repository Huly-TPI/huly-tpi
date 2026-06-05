package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnthropicChatAdapterTest {

    private ChatModel chatModel;
    private AnthropicChatAdapter adapter;

    @BeforeEach
    void setUp() {
        chatModel = mock(ChatModel.class);
        adapter = new AnthropicChatAdapter(chatModel);
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


    private ChatResponse chatResponse(String text) {
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(text);
        return chatResponse;
    }

    @Test
    void stream_shouldReturnTextDeltasFromModelStream() {
        ChatResponse first = chatResponse("hola ");
        ChatResponse second = chatResponse("mundo");
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(first, second));

        List<String> result = adapter.stream("prompt", "hola", List.of()).collectList().block();

        assertThat(result).containsExactly("hola ", "mundo");
    }

    // ── parseResponse - valid JSON ────────────────────────────────────────────

    @Test
    void chat_shouldParseValidJsonResponse() {
        String json = """
                {
                  "huly_reply": "todo bien",
                  "detected_emotion": "JOY",
                  "intensity": 8,
                  "risk_detected": false,
                  "matched_word": null
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("prompt", "hola", List.of());

        assertThat(result.content()).isEqualTo("todo bien");
        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.JOY);
        assertThat(result.intensity()).isEqualTo(8);
        assertThat(result.riskDetected()).isFalse();
        assertThat(result.matchedWord()).isNull();
    }

    @Test
    void chat_shouldParseJsonWithRiskDetectedAndMatchedWord() {
        String json = """
                {
                  "huly_reply": "cuidado",
                  "detected_emotion": "FEAR",
                  "intensity": 9,
                  "risk_detected": true,
                  "matched_word": "suicidio"
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("p", "mal", List.of());

        assertThat(result.riskDetected()).isTrue();
        assertThat(result.matchedWord()).isEqualTo("suicidio");
    }

    @Test
    void chat_shouldReturnNullIntensity_whenIntensityFieldIsMissing() {
        String json = """
                {
                  "huly_reply": "resp",
                  "detected_emotion": "CALM",
                  "risk_detected": false
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.intensity()).isNull();
    }

    @Test
    void chat_shouldReturnNullRiskDetected_whenRiskDetectedFieldIsMissing() {
        String json = """
                {
                  "huly_reply": "resp",
                  "detected_emotion": "CALM"
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.riskDetected()).isNull();
    }

    @Test
    void chat_shouldReturnNullEmotion_whenEmotionIsUnknown() {
        String json = """
                {
                  "huly_reply": "resp",
                  "detected_emotion": "EMOCION_INVALIDA",
                  "intensity": 5,
                  "risk_detected": false,
                  "matched_word": null
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.detectedEmotion()).isNull();
    }

    @Test
    void chat_shouldReturnNullEmotion_whenEmotionFieldIsBlank() {
        String json = """
                {
                  "huly_reply": "resp",
                  "detected_emotion": "",
                  "intensity": 3,
                  "risk_detected": false,
                  "matched_word": null
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.detectedEmotion()).isNull();
    }

    @Test
    void chat_shouldReturnEmotionCaseInsensitive_whenEmotionIsLowercase() {
        String json = """
                {
                  "huly_reply": "resp",
                  "detected_emotion": "sadness",
                  "intensity": 6,
                  "risk_detected": false,
                  "matched_word": null
                }""";
        givenModelReturns(json);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.detectedEmotion()).isEqualTo(EmotionType.SADNESS);
    }

    // ── parseResponse - fallback on invalid JSON ──────────────────────────────

    @Test
    void chat_shouldFallbackToPlainText_whenResponseIsNotJson() {
        String raw = "respuesta sin formato JSON";
        givenModelReturns(raw);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.content()).isEqualTo(raw);
        assertThat(result.detectedEmotion()).isNull();
    }

    @Test
    void chat_shouldFallbackToPlainText_whenJsonIsMalformed() {
        givenModelReturns("{invalid json}");

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.content()).isNotNull();
    }

    // ── extractJson ───────────────────────────────────────────────────────────

    @Test
    void chat_shouldExtractJsonFromTextWithSurroundingContent() {
        String raw = "texto previo { \"huly_reply\": \"ok\", \"detected_emotion\": null, \"intensity\": null, \"risk_detected\": null, \"matched_word\": null } texto posterior";
        givenModelReturns(raw);

        ChatReply result = adapter.chat("p", "msg", List.of());

        assertThat(result.content()).isEqualTo("ok");
    }

    // ── buildMessages ─────────────────────────────────────────────────────────

    @Test
    void chat_shouldOmitSystemMessage_whenSystemPromptIsBlank() {
        String json = "{\"huly_reply\": \"ok\", \"detected_emotion\": null, \"intensity\": null, \"risk_detected\": null, \"matched_word\": null}";
        givenModelReturns(json);

        ChatReply result = adapter.chat("", "hola", List.of());

        assertThat(result.content()).isEqualTo("ok");
    }

    @Test
    void chat_shouldOmitSystemMessage_whenSystemPromptIsNull() {
        String json = "{\"huly_reply\": \"ok\", \"detected_emotion\": null, \"intensity\": null, \"risk_detected\": null, \"matched_word\": null}";
        givenModelReturns(json);

        ChatReply result = adapter.chat(null, "hola", List.of());

        assertThat(result.content()).isEqualTo("ok");
    }

    @Test
    void chat_shouldIncludeHistoryMessagesInPrompt() {
        String json = "{\"huly_reply\": \"ok\", \"detected_emotion\": null, \"intensity\": null, \"risk_detected\": null, \"matched_word\": null}";
        givenModelReturns(json);

        List<ConversationMessage> history = List.of(
                new ConversationMessage(MessageRole.USER, "anterior", null, null, null),
                new ConversationMessage(MessageRole.ASSISTANT, "respuesta anterior", null, null, null)
        );

        ChatReply result = adapter.chat("prompt", "nuevo mensaje", history);

        assertThat(result.content()).isEqualTo("ok");
    }
}
