package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatUserIntent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatIntentDetectionServiceTest {

    private final ChatIntentDetectionService service = new ChatIntentDetectionService();

    @Test
    void detect_shouldReturnActivityRequest_whenUserAsksForActivityRecommendation() {
        assertThat(service.detect("dame una recomendacion de alguna actividad"))
                .isEqualTo(ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
        assertThat(service.detect("necesito algo para calmarme"))
                .isEqualTo(ChatUserIntent.ACTIVITY_RECOMMENDATION_REQUEST);
    }

    @Test
    void detect_shouldReturnChallengeRequest_whenUserAsksForChallenge() {
        assertThat(service.detect("quiero un reto"))
                .isEqualTo(ChatUserIntent.CHALLENGE_REQUEST);
        assertThat(service.detect("me propones un desafio para hoy?"))
                .isEqualTo(ChatUserIntent.CHALLENGE_REQUEST);
    }

    @Test
    void detect_shouldIgnoreChallengeDecisions() {
        assertThat(service.detect("Acepto este reto"))
                .isEqualTo(ChatUserIntent.NONE);
        assertThat(service.detect("Rechazo este reto por ahora"))
                .isEqualTo(ChatUserIntent.NONE);
    }
}
