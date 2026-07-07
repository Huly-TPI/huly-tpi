package com.huly.backend.infrastructure.adapter.anthropic;

import com.huly.backend.domain.model.enums.MentalLoadBucket;
import com.huly.backend.domain.port.pending.MentalLoadEstimate;
import com.huly.backend.domain.port.pending.MentalLoadEstimationInput;
import com.huly.backend.domain.port.pending.MentalLoadEstimationPort;
import com.huly.backend.domain.service.pending.HeuristicMentalLoadEstimator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Primary
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "anthropic")
public class AnthropicMentalLoadEstimationAdapter implements MentalLoadEstimationPort {

    private final ChatClient chatClient;
    private final HeuristicMentalLoadEstimator heuristicEstimator;
    private final Resource mentalLoadPrompt;

    public AnthropicMentalLoadEstimationAdapter(
            ChatClient chatClient,
            HeuristicMentalLoadEstimator heuristicEstimator,
            @Value("classpath:/prompts/mental-load-estimation.st") Resource mentalLoadPrompt) {
        this.chatClient = chatClient;
        this.heuristicEstimator = heuristicEstimator;
        this.mentalLoadPrompt = mentalLoadPrompt;
    }

    @Override
    public MentalLoadEstimate estimate(MentalLoadEstimationInput input) {
        try {
            String systemPrompt = readPrompt();
            String userMessage = buildUserMessage(input);

            MentalLoadLlmResult result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .call()
                    .entity(MentalLoadLlmResult.class);

            MentalLoadBucket bucket = parseBucket(result);
            return heuristicEstimator.estimateFromSignals(bucket, input);
        } catch (Exception e) {
            log.warn("No se pudo estimar la carga mental con IA, usando fallback heurístico: {}", e.getMessage());
            return heuristicEstimator.estimateWithoutAi(input);
        }
    }

    private String readPrompt() {
        if (mentalLoadPrompt == null) {
            return "";
        }
        try {
            return mentalLoadPrompt.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Error leyendo el prompt de carga mental", e);
            return "";
        }
    }

    private String buildUserMessage(MentalLoadEstimationInput input) {
        return "Titulo: " + input.title()
                + "\nDescripcion: " + (input.description() == null ? "(sin descripcion)" : input.description())
                + "\nVence en: " + (input.daysUntilDue() == null ? "sin fecha" : input.daysUntilDue() + " dias")
                + "\nDuracion estimada: " + (input.estimatedDuration() == null ? "sin definir" : input.estimatedDuration())
                + "\nCategoria: " + (input.category() == null ? "sin categoria" : input.category())
                + "\nSubtareas: " + input.subtaskCount();
    }

    private MentalLoadBucket parseBucket(MentalLoadLlmResult result) {
        if (result == null || result.bucket() == null) {
            return MentalLoadBucket.MEDIUM;
        }
        try {
            return MentalLoadBucket.valueOf(result.bucket().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MentalLoadBucket.MEDIUM;
        }
    }

    record MentalLoadLlmResult(String bucket, String reason) {}
}
