package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.enums.EmotionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de construir el prompt enriquecido que se envía al modelo de lenguaje,
 * combinando el prompt base con las instrucciones de formato y las palabras de riesgo activas.
 */
@Service
@RequiredArgsConstructor
public class PromptBuilderService {

    /**
     * Construye el prompt enriquecido a partir del prompt base y la lista de palabras de riesgo.
     *
     * <p>El prompt resultante incluye:
     * <ul>
     *   <li>El prompt base del sistema.</li>
     *   <li>Las instrucciones de formato JSON que debe seguir el modelo.</li>
     *   <li>La lista de palabras y frases de riesgo activas, si las hay.</li>
     * </ul>
     *
     * @param basePrompt prompt base obtenido desde la configuración del sistema
     * @param riskWords  lista de palabras de riesgo activas a incluir en el prompt
     * @return prompt completo y enriquecido listo para enviarse al modelo de lenguaje
     */
    public String buildEnrichedPrompt(String basePrompt, List<RiskWord> riskWords) {
        StringBuilder sb = new StringBuilder(basePrompt);
        appendResponseInstructions(sb);
        appendRiskWords(sb, riskWords);
        return sb.toString();
    }

    /**
     * Agrega al prompt las instrucciones de formato JSON que debe respetar el modelo en su respuesta.
     *
     * @param sb {@link StringBuilder} sobre el cual se agregan las instrucciones
     */
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

    /**
     * Agrega al prompt la sección de palabras y frases de riesgo, si la lista no está vacía.
     *
     * @param sb        {@link StringBuilder} sobre el cual se agregan las palabras de riesgo
     * @param riskWords lista de palabras de riesgo activas a incluir
     */
    private void appendRiskWords(StringBuilder sb, List<RiskWord> riskWords) {
        if (riskWords.isEmpty()) return;

        sb.append("\n=== PALABRAS Y FRASES DE RIESGO ===");
        sb.append("\nAnalizá el CONTEXTO del mensaje, no solo coincidencias literales:");
        riskWords.forEach(rw -> appendRiskWordEntry(sb, rw));
    }

    /**
     * Agrega al prompt una entrada individual de palabra de riesgo con su severidad
     * y descripción opcional.
     *
     * @param sb {@link StringBuilder} sobre el cual se agrega la entrada
     * @param rw palabra de riesgo a representar en el prompt
     */
    private void appendRiskWordEntry(StringBuilder sb, RiskWord rw) {
        sb.append("\n- \"").append(rw.getWord()).append("\" [").append(rw.getSeverity()).append("]");
        if (rw.getDescription() != null && !rw.getDescription().isBlank()) {
            sb.append(" — ").append(rw.getDescription());
        }
    }

    /**
     * Construye una cadena con todos los valores del enum {@link EmotionType} separados por {@code |},
     * para ser usada como referencia en las instrucciones del prompt.
     *
     * @return cadena con los valores de {@link EmotionType} separados por {@code |}
     */
    private String buildEmotionList() {
        return Arrays.stream(EmotionType.values())
                .map(EmotionType::name)
                .collect(Collectors.joining("|"));
    }
}