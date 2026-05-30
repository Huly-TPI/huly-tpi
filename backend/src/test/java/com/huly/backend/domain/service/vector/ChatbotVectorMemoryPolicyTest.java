package com.huly.backend.domain.service.vector;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatbotVectorMemoryPolicyTest {

    private final ChatbotVectorMemoryPolicy policy = new ChatbotVectorMemoryPolicy();

    @Test
    void shouldRemember_shouldReturnTrueForUsefulMemorySignals() {
        assertThat(policy.shouldRemember("me gusta jugar a la play")).isTrue();
        assertThat(policy.shouldRemember("mi nombre es sergio")).isTrue();
    }

    @Test
    void shouldRemember_shouldReturnFalseForTrivialMessages() {
        assertThat(policy.shouldRemember("hola")).isFalse();
        assertThat(policy.shouldRemember("gracias")).isFalse();
    }
}
