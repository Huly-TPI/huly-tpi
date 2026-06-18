package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.chat.ChatPreferenceDetectionResult;
import com.huly.backend.domain.model.enums.ChatPreferenceExpectedField;
import com.huly.backend.domain.model.enums.ChatPreferenceMessageType;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.domain.provider.ChatPreferenceExtractionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ChatPreferenceResolutionService {

    private static final double MIN_AI_CONFIDENCE = 0.85;

    private final ChatPreferenceDetectionService deterministicDetectionService;
    private final ChatPreferenceExtractionPort extractionPort;

    public ChatPreferenceDetectionResult resolve(
            String message,
            ChatPreferenceExpectedField expectedField) {
        if (expectedField != ChatPreferenceExpectedField.ANY) {
            ChatPreferenceDetectionResult semanticResult;
            try {
                semanticResult = extractionPort.extract(message, expectedField);
            } catch (RuntimeException ignored) {
                return ChatPreferenceDetectionResult.unrelated();
            }
            return validateSemanticResult(semanticResult);
        }

        if (hasPreferenceChangeSignal(message)) {
            ChatPreferenceDetectionResult semanticResult;
            try {
                semanticResult = extractionPort.extract(message, ChatPreferenceExpectedField.ANY);
            } catch (RuntimeException ignored) {
                return ChatPreferenceDetectionResult.unrelated();
            }
            return validateSemanticResult(semanticResult);
        }

        return ChatPreferenceDetectionResult.unrelated();
    }

    private boolean hasPreferenceChangeSignal(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalized = normalize(message);
        return normalized.contains("decime")
                || normalized.contains("dime")
                || normalized.contains("llamame")
                || normalized.contains("nombre")
                || normalized.contains("apodo")
                || normalized.contains("cambia mi")
                || normalized.contains("cambiar mi")
                || normalized.contains("hablame")
                || normalized.contains("tono")
                || normalized.contains("estilo")
                || normalized.contains("respondeme")
                || normalized.contains("seas")
                || normalized.contains("se mas")
                || normalized.contains("se menos")
                || normalized.contains("cambia el");
    }

    private ChatPreferenceDetectionResult validateSemanticResult(
            ChatPreferenceDetectionResult semanticResult) {
        if (semanticResult == null || semanticResult.confidence() < MIN_AI_CONFIDENCE) {
            return ChatPreferenceDetectionResult.unrelated();
        }

        String preferredName = deterministicDetectionService
                .validatePreferredName(semanticResult.preferredName())
                .orElse(null);
        CommunicationStyle style = semanticResult.communicationStyle();
        if (preferredName == null && style == null) {
            return ChatPreferenceDetectionResult.unrelated();
        }
        ChatPreferenceMessageType type = semanticResult.messageType() != null
                ? semanticResult.messageType()
                : ChatPreferenceMessageType.MIXED;
        return new ChatPreferenceDetectionResult(
                preferredName,
                style,
                type,
                semanticResult.confidence());
    }

    private String normalize(String value) {
        String compact = value == null
                ? ""
                : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        return Normalizer.normalize(compact, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
    }
}
