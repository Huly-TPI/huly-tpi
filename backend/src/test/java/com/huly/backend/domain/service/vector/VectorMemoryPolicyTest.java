package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorMemoryPolicyTest {

    private final VectorMemoryProperties properties = new VectorMemoryProperties();
    private final VectorMemoryPolicy policy = new VectorMemoryPolicy(properties);

    @Test
    void normalizeContent_shouldTrimAndCollapseSpaces() {
        String result = policy.normalizeContent("  hola   mundo   ");

        assertThat(result).isEqualTo("hola mundo");
    }

    @Test
    void shouldRemember_shouldRejectTrivialMessages() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "hola",
                "conv-1",
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "hola")).isFalse();
    }

    @Test
    void shouldRemember_shouldRejectSensitiveMessages() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "tengo un diagnostico",
                "conv-1",
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "tengo un diagnostico")).isFalse();
    }

    @Test
    void shouldRemember_shouldAcceptUsefulMessages() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "me gusta jugar a la play",
                "conv-1",
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "me gusta jugar a la play")).isTrue();
    }

    @Test
    void shouldRemember_shouldRejectShortMessageBelowGlobalMinimumForChatbot() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "conv-1",
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                "ab",
                "conv-1",
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "ab")).isFalse();
    }

    @Test
    void shouldRemember_shouldAcceptGuidedLanternsContentAboveMinLength() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_LANTERNS,
                null,
                null,
                null,
                "me siento muy triste hoy",
                null,
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "me siento muy triste hoy")).isTrue();
    }

    @Test
    void shouldRemember_shouldRejectGuidedLanternsContentBelowMinLength() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_LANTERNS,
                null,
                null,
                null,
                "ab",
                null,
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "ab")).isFalse();
    }

    @Test
    void shouldRemember_shouldAcceptShortGuidedLanternMessageUsingSourceSpecificMinimum() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_LANTERNS,
                null,
                null,
                null,
                "ansiedad",
                null,
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "ansiedad")).isTrue();
    }

    @Test
    void shouldRemember_shouldStillRejectVeryShortGuidedLanternMessage() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_LANTERNS,
                null,
                null,
                null,
                "ok",
                null,
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "ok")).isFalse();
    }

    @Test
    void validateAndNormalizeQuery_shouldRejectInvalidLimitAndBlankQuery() {
        SearchVectorMemoryQuery invalidLimit = new SearchVectorMemoryQuery(
                1L,
                VectorMemorySource.CHATBOT,
                "consulta",
                0,
                0.65
        );

        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(invalidLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit must be between 1");

        SearchVectorMemoryQuery blankQuery = new SearchVectorMemoryQuery(
                1L,
                VectorMemorySource.CHATBOT,
                "   ",
                5,
                0.65
        );

        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(blankQuery))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("query is required");
    }

    @Test
    void shouldRemember_shouldBypassFiltersForStructuralChatbotMemories() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.CHATBOT,
                "challenge-decision-1",
                "CHALLENGE_DECISION",
                "CHALLENGE_DECISION",
                "El usuario rechazo el reto: Mirá a tu alrededor.",
                "conv-1",
                null,
                null
        );

        assertThat(policy.shouldRemember(command, command.content())).isTrue();
    }

    @Test
    void normalizeContent_shouldHandleNullAndMaxLength() {
        assertThat(policy.normalizeContent(null)).isEmpty();

        properties.setMaxContentLength(5);
        assertThat(policy.normalizeContent("abcdefgh")).isEqualTo("abcde");
        properties.setMaxContentLength(500);
    }

    @Test
    void validateSaveCommand_shouldThrowOnInvalidInputs() {
        assertThatThrownBy(() -> policy.validateSaveCommand(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vector memory command is required");

        SaveVectorMemoryCommand commandNullUser = new SaveVectorMemoryCommand(null, VectorMemorySource.CHATBOT, "id", "src", "type", "content", null, null, null);
        assertThatThrownBy(() -> policy.validateSaveCommand(commandNullUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId is required");

        SaveVectorMemoryCommand commandNullSourceType = new SaveVectorMemoryCommand(1L, null, "id", "src", "type", "content", null, null, null);
        assertThatThrownBy(() -> policy.validateSaveCommand(commandNullSourceType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceType is required");

        SaveVectorMemoryCommand commandBlankContent = new SaveVectorMemoryCommand(1L, VectorMemorySource.CHATBOT, "id", "src", "type", "   ", null, null, null);
        assertThatThrownBy(() -> policy.validateSaveCommand(commandBlankContent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content is required");
    }

    @Test
    void validateAndNormalizeQuery_shouldThrowOnInvalidInputs() {
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vector memory query is required");

        SearchVectorMemoryQuery nullUser = new SearchVectorMemoryQuery(null, VectorMemorySource.CHATBOT, "query", 5, 0.5);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(nullUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("userId is required");

        SearchVectorMemoryQuery nullSource = new SearchVectorMemoryQuery(1L, null, "query", 5, 0.5);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(nullSource))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sourceType is required");

        SearchVectorMemoryQuery nullLimit = new SearchVectorMemoryQuery(1L, VectorMemorySource.CHATBOT, "query", null, 0.5);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(nullLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit must be between");

        SearchVectorMemoryQuery highLimit = new SearchVectorMemoryQuery(1L, VectorMemorySource.CHATBOT, "query", 100, 0.5);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(highLimit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("limit must be between");

        SearchVectorMemoryQuery nullThreshold = new SearchVectorMemoryQuery(1L, VectorMemorySource.CHATBOT, "query", 5, null);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(nullThreshold))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("similarityThreshold must be between");

        SearchVectorMemoryQuery lowThreshold = new SearchVectorMemoryQuery(1L, VectorMemorySource.CHATBOT, "query", 5, -0.1);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(lowThreshold))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("similarityThreshold must be between");

        SearchVectorMemoryQuery highThreshold = new SearchVectorMemoryQuery(1L, VectorMemorySource.CHATBOT, "query", 5, 1.1);
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(highThreshold))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("similarityThreshold must be between");
    }

}
