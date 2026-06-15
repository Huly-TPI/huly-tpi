package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorMemoryPolicyTest {

    private final VectorMemoryProperties properties = new VectorMemoryProperties();
    private final VectorMemoryPolicy policy = new VectorMemoryPolicy(
            properties,
            List.of(new ChatbotVectorMemoryPolicy(), new GuidedCloudsVectorMemoryPolicy()),
            new DefaultVectorMemorySourcePolicy()
    );

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
    void shouldRemember_shouldAcceptShortMessageAtGuidedCloudsMinimum() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_CLOUDS,
                null,
                null,
                null,
                "ab",
                null,
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "ab")).isTrue();
    }

    @Test
    void shouldRemember_shouldRejectMessageBelowGuidedCloudsMinimum() {
        SaveVectorMemoryCommand command = new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.GUIDED_CLOUDS,
                null,
                null,
                null,
                "a",
                null,
                null,
                null
        );

        assertThat(policy.shouldRemember(command, "a")).isFalse();
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

        // Even though this content doesn't contain standard chatbot signals like "me llamo",
        // it should still be remembered because it's a structural memory (contentType = "CHALLENGE_DECISION")
        assertThat(policy.shouldRemember(command, command.content())).isTrue();
    }
}
