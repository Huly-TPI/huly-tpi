package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.provider.LLMChatPort;
import com.huly.backend.domain.useCase.cloudRecommendation.GetCloudRecommendationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCloudRecommendationUseCaseTest {

    @Mock
    private LLMChatPort llmChatPort;

    @InjectMocks
    private GetCloudRecommendationUseCase useCase;

    private static final String DIARY_JSON = """
            {
              "activity_type": "diary",
              "action_id": "diary",
              "title": "Escribí en tu diario",
              "description": "Plasmar tus emociones puede ayudarte.",
              "redirect_url": "/diary"
            }
            """;

    private static final String CLOUDS_JSON = """
            {
              "activity_type": "clouds",
              "action_id": "clouds",
              "title": "Soltar más pensamientos",
              "description": "Seguí liberando lo que sentís.",
              "redirect_url": "/clouds"
            }
            """;

    private static final String BREATHING_JSON = """
            {
              "activity_type": "breathing",
              "action_id": "breathing",
              "title": "Respirá profundo",
              "description": "Una respiración guiada puede ayudarte a calmarte.",
              "redirect_url": "/guided-breathing"
            }
            """;

    private static final String BUBBLES_JSON = """
            {
              "activity_type": "bubbles",
              "action_id": "bubbles",
              "title": "Explotá burbujas",
              "description": "Liberar tensión con burbujas puede ser muy útil.",
              "redirect_url": "/bubbles"
            }
            """;

    @Test
    void execute_shouldReturnDiaryRecommendation_whenLlmReturnsDiaryJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of(DIARY_JSON));

        CloudRecommendation result = useCase.execute(List.of("me siento muy triste y no sé por qué"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.description()).isEqualTo("Plasmar tus emociones puede ayudarte.");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnCloudsRecommendation_whenLlmReturnsCloudsJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of(CLOUDS_JSON));

        CloudRecommendation result = useCase.execute(List.of("estoy bien"));

        assertThat(result.activityType()).isEqualTo("clouds");
        assertThat(result.actionId()).isEqualTo("clouds");
        assertThat(result.redirectUrl()).isEqualTo("/clouds");
    }

    @Test
    void execute_shouldReturnBreathingRecommendation_whenLlmReturnsBreathingJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of(BREATHING_JSON));

        CloudRecommendation result = useCase.execute(List.of("me siento muy ansioso"));

        assertThat(result.activityType()).isEqualTo("breathing");
        assertThat(result.actionId()).isEqualTo("breathing");
        assertThat(result.title()).isEqualTo("Respirá profundo");
        assertThat(result.redirectUrl()).isEqualTo("/guided-breathing");
    }

    @Test
    void execute_shouldReturnBubblesRecommendation_whenLlmReturnsBubblesJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of(BUBBLES_JSON));

        CloudRecommendation result = useCase.execute(List.of("tengo mucha tensión acumulada"));

        assertThat(result.activityType()).isEqualTo("bubbles");
        assertThat(result.actionId()).isEqualTo("bubbles");
        assertThat(result.title()).isEqualTo("Explotá burbujas");
        assertThat(result.redirectUrl()).isEqualTo("/bubbles");
    }

    @Test
    void execute_shouldFallbackToDiary_whenActivityTypeIsUnknown() {
        String unknownJson = """
                {
                  "activity_type": "unknown",
                  "action_id": "unknown",
                  "title": "Actividad desconocida",
                  "description": "Descripción.",
                  "redirect_url": "/unknown"
                }
                """;
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of(unknownJson));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldExtractJsonFromResponseWithSurroundingText() {
        String responseWithExtraText = "Aquí está mi recomendación:\n" + DIARY_JSON + "\n¡Espero que te ayude!";
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of(responseWithExtraText));

        CloudRecommendation result = useCase.execute(List.of("pensamiento de prueba"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnFallback_whenLlmThrowsException() {
        when(llmChatPort.chat(any(), any(), anyList())).thenThrow(new RuntimeException("Proveedor IA no disponible"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnFallback_whenLlmReturnsInvalidJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of("esto no es json válido"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldUseDefaultValues_whenLlmReturnsEmptyJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(ChatReply.of("{}"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldSendThoughtAsUserMessage() {
        when(llmChatPort.chat(any(), eq("no puedo dejar de pensar en lo que pasó"), anyList()))
                .thenReturn(ChatReply.of(DIARY_JSON));

        CloudRecommendation result = useCase.execute(List.of("no puedo dejar de pensar en lo que pasó"));

        assertThat(result.activityType()).isEqualTo("diary");
    }
}
