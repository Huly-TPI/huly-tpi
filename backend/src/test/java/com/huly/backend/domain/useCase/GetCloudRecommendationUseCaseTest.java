package com.huly.backend.domain.useCase;

import com.huly.backend.domain.model.ChatReply;
import com.huly.backend.domain.model.CloudRecommendation;
import com.huly.backend.domain.provider.LLMChatPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void execute_shouldReturnDiaryRecommendation_whenLlmReturnsDiaryJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(new ChatReply(DIARY_JSON));

        CloudRecommendation result = useCase.execute(List.of("me siento muy triste y no sé por qué"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.description()).isEqualTo("Plasmar tus emociones puede ayudarte.");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnCloudsRecommendation_whenLlmReturnsCloudsJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(new ChatReply(CLOUDS_JSON));

        CloudRecommendation result = useCase.execute(List.of("estoy bien"));

        assertThat(result.activityType()).isEqualTo("clouds");
        assertThat(result.actionId()).isEqualTo("clouds");
        assertThat(result.redirectUrl()).isEqualTo("/clouds");
    }

    @Test
    void execute_shouldExtractJsonFromResponseWithSurroundingText() {
        String responseWithExtraText = "Aquí está mi recomendación:\n" + DIARY_JSON + "\n¡Espero que te ayude!";
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(new ChatReply(responseWithExtraText));

        CloudRecommendation result = useCase.execute(List.of("pensamiento de prueba"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnFallback_whenLlmThrowsException() {
        when(llmChatPort.chat(any(), any(), anyList())).thenThrow(new RuntimeException("Ollama no disponible"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldReturnFallback_whenLlmReturnsInvalidJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(new ChatReply("esto no es json válido"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldUseDefaultValues_whenLlmReturnsEmptyJson() {
        when(llmChatPort.chat(any(), any(), anyList())).thenReturn(new ChatReply("{}"));

        CloudRecommendation result = useCase.execute(List.of("pensamiento"));

        assertThat(result.activityType()).isEqualTo("diary");
        assertThat(result.actionId()).isEqualTo("diary");
        assertThat(result.title()).isEqualTo("Escribí en tu diario");
        assertThat(result.redirectUrl()).isEqualTo("/diary");
    }

    @Test
    void execute_shouldSendThoughtAsUserMessage() {
        when(llmChatPort.chat(any(), eq("no puedo dejar de pensar en lo que pasó"), anyList()))
                .thenReturn(new ChatReply(DIARY_JSON));

        CloudRecommendation result = useCase.execute(List.of("no puedo dejar de pensar en lo que pasó"));

        assertThat(result.activityType()).isEqualTo("diary");
    }
}
