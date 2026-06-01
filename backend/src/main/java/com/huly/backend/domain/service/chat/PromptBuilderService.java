package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.vector.VectorMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de construir prompts enriquecidos para el modelo de lenguaje.
 */
@Service
@RequiredArgsConstructor
public class PromptBuilderService {

    public String buildEnrichedPrompt(String basePrompt, List<RiskWord> riskWords) {
        return buildEnrichedPrompt(basePrompt, riskWords, Collections.emptyList());
    }

    public String buildEnrichedPrompt(String basePrompt, List<RiskWord> riskWords, List<VectorMemory> memories) {
        StringBuilder sb = basePromptBuilder(basePrompt);
        appendVectorMemories(sb, memories);
        appendResponseInstructions(sb);
        appendRiskWords(sb, riskWords);
        return sb.toString();
    }

    public String buildStreamingPrompt(String basePrompt, List<RiskWord> riskWords, List<VectorMemory> memories) {
        StringBuilder sb = basePromptBuilder(basePrompt);
        appendVectorMemories(sb, memories);
        appendStreamingInstructions(sb);
        appendRiskWords(sb, riskWords);
        return sb.toString();
    }

    public String buildMetadataPrompt(String basePrompt, List<RiskWord> riskWords, List<VectorMemory> memories) {
        StringBuilder sb = basePromptBuilder(basePrompt);
        appendVectorMemories(sb, memories);
        appendMetadataInstructions(sb);
        appendRiskWords(sb, riskWords);
        return sb.toString();
    }

    private StringBuilder basePromptBuilder(String basePrompt) {
        return new StringBuilder(basePrompt == null ? "" : basePrompt);
    }

    private void appendVectorMemories(StringBuilder sb, List<VectorMemory> memories) {
        if (memories == null || memories.isEmpty()) {
            return;
        }

        sb.append("\n\n=== INFORMACION RECORDADA DEL USUARIO ===");
        sb.append("\nUsa esta informacion solo si ayuda a responder. No la menciones de forma forzada.");
        memories.stream()
                .map(VectorMemory::content)
                .filter(content -> content != null && !content.isBlank())
                .forEach(content -> sb.append("\n- ").append(content));
    }

    private void appendResponseInstructions(StringBuilder sb) {
        sb.append("\n\n=== INSTRUCCIONES DE RESPUESTA ===");
        sb.append("\nRespondé SIEMPRE con un JSON válido con exactamente este formato (sin texto fuera del JSON):");
        sb.append("\n{");
        sb.append("\n  \"huly_reply\": \"<tu respuesta empática>\",");
        sb.append("\n  \"detected_emotion\": \"<").append(buildEmotionList()).append(">\",");
        sb.append("\n  \"intensity\": <número del 1 al 10>,");
        sb.append("\n  \"risk_detected\": <true|false>,");
        sb.append("\n  \"matched_word\": \"<frase de riesgo detectada, o null>\"");
        sb.append("\n}");
    }

    private void appendStreamingInstructions(StringBuilder sb) {
        sb.append("\n\n=== INSTRUCCIONES DE RESPUESTA EN STREAMING ===");
        sb.append("\nRespondé en texto natural, cálido y directo. No devuelvas JSON ni markdown técnico.");
        sb.append("\nLa metadata emocional y de riesgo se calculará después; durante el stream solo escribí la respuesta para el usuario.");
    }

    private void appendMetadataInstructions(StringBuilder sb) {
        sb.append("\n\n=== INSTRUCCIONES DE ANALISIS ===");
        sb.append("\nAnalizá el mensaje del usuario y respondé únicamente con un JSON válido con exactamente este formato:");
        sb.append("\n{");
        sb.append("\n  \"huly_reply\": \"\",");
        sb.append("\n  \"detected_emotion\": \"<").append(buildEmotionList()).append(">\",");
        sb.append("\n  \"intensity\": <número del 1 al 10>,");
        sb.append("\n  \"risk_detected\": <true|false>,");
        sb.append("\n  \"matched_word\": \"<frase de riesgo detectada, o null>\"");
        sb.append("\n}");
    }

    private void appendRiskWords(StringBuilder sb, List<RiskWord> riskWords) {
        if (riskWords == null || riskWords.isEmpty()) return;

        sb.append("\n=== PALABRAS Y FRASES DE RIESGO ===");
        sb.append("\nAnalizá el CONTEXTO del mensaje, no solo coincidencias literales:");
        riskWords.forEach(rw -> appendRiskWordEntry(sb, rw));
    }

    private void appendRiskWordEntry(StringBuilder sb, RiskWord rw) {
        sb.append("\n- \"").append(rw.getWord()).append("\" [").append(rw.getSeverity()).append("]");
        if (rw.getDescription() != null && !rw.getDescription().isBlank()) {
            sb.append(" - ").append(rw.getDescription());
        }
    }

    private String buildEmotionList() {
        return Arrays.stream(EmotionType.values())
                .map(EmotionType::name)
                .collect(Collectors.joining("|"));
    }
}
