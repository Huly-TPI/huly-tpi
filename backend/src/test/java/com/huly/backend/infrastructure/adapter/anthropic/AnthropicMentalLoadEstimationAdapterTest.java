package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import com.huly.backend.domain.service.pending.HeuristicMentalLoadEstimator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnthropicMentalLoadEstimationAdapterTest {

    private ChatClient chatClient;
    private AnthropicMentalLoadEstimationAdapter adapter;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        Resource prompt = new ByteArrayResource("Sos Huly...".getBytes());
        adapter = new AnthropicMentalLoadEstimationAdapter(chatClient, new HeuristicMentalLoadEstimator(), prompt);
    }

    @Test
    void estimate_shouldReturnEstimateDerivedFromLlmBucket() {
        AnthropicMentalLoadEstimationAdapter.MentalLoadLlmResult llmResult =
                new AnthropicMentalLoadEstimationAdapter.MentalLoadLlmResult("HIGH", "Vence pronto");

        when(chatClient.prompt().system(anyString()).user(anyString()).call()
                .entity(AnthropicMentalLoadEstimationAdapter.MentalLoadLlmResult.class))
                .thenReturn(llmResult);

        MentalLoadEstimate estimate = adapter.estimate(input());

        assertThat(estimate.bucket()).isEqualTo(MentalLoadBucket.HIGH);
        assertThat(estimate.score()).isBetween(0.0, 1.0);
    }

    @Test
    void estimate_shouldFallbackToHeuristic_whenExceptionOccurs() {
        when(chatClient.prompt().system(anyString()).user(anyString()).call()
                .entity(AnthropicMentalLoadEstimationAdapter.MentalLoadLlmResult.class))
                .thenThrow(new RuntimeException("timeout"));

        MentalLoadEstimate estimate = adapter.estimate(input());

        assertThat(estimate).isNotNull();
        assertThat(estimate.score()).isBetween(0.0, 1.0);
    }

    private MentalLoadEstimationInput input() {
        return new MentalLoadEstimationInput("Tarea", "desc", null, 1, null, null, 0);
    }
}
