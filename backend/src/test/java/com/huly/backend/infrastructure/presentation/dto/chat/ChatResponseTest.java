package com.huly.backend.infrastructure.presentation.dto.chat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseTest {

    @Test
    void chatResponse_shouldCreateWithAllFields() {
        ChatResponse.Metadata metadata = new ChatResponse.Metadata(true, "suicidio");
        ChatResponse.SuggestedAction action = new ChatResponse.SuggestedAction(
                "EXERCISE", "act-1", "Respiración", "Técnica de calma", "/ejercicios/1");
        ChatResponse.GeneratedChallenge challenge = new ChatResponse.GeneratedChallenge(
                "Desafío del día", "Sal a caminar 10 minutos");

        ChatResponse response = new ChatResponse("todo bien", "JOY", 8, action, challenge, metadata);

        assertThat(response.hulyReply()).isEqualTo("todo bien");
        assertThat(response.detectedEmotion()).isEqualTo("JOY");
        assertThat(response.intensity()).isEqualTo(8);
    }

    @Test
    void metadata_shouldExposeAllFields() {
        ChatResponse.Metadata metadata = new ChatResponse.Metadata(true, "palabra");

        assertThat(metadata.riskDetected()).isTrue();
        assertThat(metadata.matchedWord()).isEqualTo("palabra");
    }

    @Test
    void metadata_shouldHandleNullFields() {
        ChatResponse.Metadata metadata = new ChatResponse.Metadata(null, null);

        assertThat(metadata.riskDetected()).isNull();
        assertThat(metadata.matchedWord()).isNull();
    }

    @Test
    void suggestedAction_shouldExposeAllFields() {
        ChatResponse.SuggestedAction action = new ChatResponse.SuggestedAction(
                "EXERCISE", "act-99", "Meditación", "Práctica de mindfulness", "/meditacion");

        assertThat(action.type()).isEqualTo("EXERCISE");
        assertThat(action.actionId()).isEqualTo("act-99");
        assertThat(action.title()).isEqualTo("Meditación");
        assertThat(action.description()).isEqualTo("Práctica de mindfulness");
        assertThat(action.actionUrl()).isEqualTo("/meditacion");
        assertThat(action.emotionalEventId()).isNull();
    }

    @Test
    void generatedChallenge_shouldExposeAllFields() {
        ChatResponse.GeneratedChallenge challenge = new ChatResponse.GeneratedChallenge(
                "Reto semanal", "Escribí en tu diario cada día");

        assertThat(challenge.title()).isEqualTo("Reto semanal");
        assertThat(challenge.description()).isEqualTo("Escribí en tu diario cada día");
    }

    @Test
    void chatResponse_shouldHandleNullOptionalFields() {
        ChatResponse response = new ChatResponse("respuesta", null, null, null, null, null);

        assertThat(response.hulyReply()).isEqualTo("respuesta");
        assertThat(response.detectedEmotion()).isNull();
        assertThat(response.intensity()).isNull();
        assertThat(response.suggestedAction()).isNull();
        assertThat(response.generatedChallenge()).isNull();
        assertThat(response.metadata()).isNull();
    }
}
