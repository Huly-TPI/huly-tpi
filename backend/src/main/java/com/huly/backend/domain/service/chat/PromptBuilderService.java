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

    public String buildEmotionalAnalysisPrompt(String basePrompt, List<VectorMemory> memories) {
        StringBuilder sb = basePromptBuilder(basePrompt);
        appendVectorMemories(sb, memories);
        appendEmotionalAnalysisInstructions(sb);
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
        sb.append("\n  \"matched_word\": \"<frase de riesgo detectada, o null>\",");
        sb.append("\n  \"generated_challenge\": <objeto con title y description, o null>");
        sb.append("\n}");
        sb.append("\n");
        sb.append("\nReglas para generated_challenge:");
        sb.append("\n- Incluilo SOLO cuando el contexto de la conversación lo justifique genuinamente: el usuario enfrenta una dificultad concreta, expresó una emoción negativa de intensidad media-alta (>= 5), o mencionó una situación que se beneficiaría de un reto personal.");
        sb.append("\n- El reto debe ser específico, alcanzable y relacionado con lo que el usuario está viviendo.");
        sb.append("\n- Si no corresponde un reto, devolvé null.");
        sb.append("\n- Formato cuando corresponde: { \"title\": \"<título corto>\", \"description\": \"<descripción accionable en 1-2 oraciones>\" }");
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
        sb.append("\n  \"matched_word\": \"<frase de riesgo detectada, o null>\",");
        sb.append("\n  \"generated_challenge\": <objeto con title y description, o null>");
        sb.append("\n}");
        sb.append("\n");
        sb.append("\nReglas para generated_challenge:");
        sb.append("\n- Incluilo SOLO cuando el contexto lo justifique: emoción negativa de intensidad >= 5 o situación concreta que se beneficiaría de un reto personal.");
        sb.append("\n- Si no corresponde, devolvé null.");
        sb.append("\n- Formato cuando corresponde: { \"title\": \"<título corto>\", \"description\": \"<descripción accionable en 1-2 oraciones>\" }");
    }

    private void appendEmotionalAnalysisInstructions(StringBuilder sb) {
        sb.append("\n\n=== ANALISIS EMOCIONAL ESTRUCTURADO ===");
        sb.append("\nAnaliza el mensaje actual usando tambien la informacion recordada del usuario y el historial conversacional disponible.");
        sb.append("\nTu tarea NO es responder al usuario. Tu tarea es producir un analisis emocional estructurado para decidir si conviene recomendar una actividad de bienestar.");
        sb.append("\nNo todos los mensajes requieren recomendacion. Mensajes casuales, saludos, agradecimientos o informacion neutra no deben recomendar actividad.");
        sb.append("\nRecomenda actividad solo si hay senales claras de malestar, ansiedad, tristeza, estres, sobrepensamiento, duelo, bloqueo emocional, baja motivacion o necesidad de regulacion.");
        sb.append("\nUsa los recuerdos del usuario solo si son relevantes. Si el usuario venia bien y ahora expresa una recaida, reflejalo en intensidad y VAD.");
        sb.append("\nEl VAD representa el estado emocional actual: valence negativo = malestar/tristeza, positivo = bienestar; arousal bajo = apagado/cansado, alto = activado/ansioso; dominance bajo = sin control/abrumado, alto = con control.");
        sb.append("\nUsa detectedEmotion con uno de estos valores reales del enum: ").append(buildEmotionList()).append(".");
        sb.append("\nSi pensarias en TRISTEZA, usa SADNESS; si pensarias en ANSIEDAD, usa ANXIETY; si pensarias en DUELO, usa GRIEF.");
        sb.append("\nconfidence e intensity deben estar entre 0.0 y 1.0. valence, arousal y dominance deben estar entre -1.0 y 1.0.");
        sb.append("\nResponde unicamente JSON valido, sin markdown ni texto fuera del JSON:");
        sb.append("\n{");
        sb.append("\n  \"shouldRecommend\": true,");
        sb.append("\n  \"detectedEmotion\": \"SADNESS\",");
        sb.append("\n  \"confidence\": 0.92,");
        sb.append("\n  \"valence\": -0.85,");
        sb.append("\n  \"arousal\": 0.35,");
        sb.append("\n  \"dominance\": -0.75,");
        sb.append("\n  \"intensity\": 0.88,");
        sb.append("\n  \"userGoal\": \"sentirse acompanado y aliviar tristeza\",");
        sb.append("\n  \"shortReason\": \"El usuario expresa una perdida significativa y bajo estado de animo.\"");
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
