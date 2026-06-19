package com.huly.backend.domain.service.chat;

import com.huly.backend.domain.model.RiskWord;
import com.huly.backend.domain.model.chat.ChatPersonalizationContext;
import com.huly.backend.domain.model.chat.ChatUserIntent;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.vector.VectorMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptBuilderService {

    public String buildEnrichedPrompt(
            String basePrompt,
            List<RiskWord> riskWords,
            List<VectorMemory> memories,
            SuggestedChatAction suggestedAction,
            ChatUserIntent userIntent,
            ChatPersonalizationContext personalization
    ) {
        StringBuilder sb = basePromptBuilder(basePrompt);
        appendConversationPreferences(sb, personalization);
        appendVectorMemories(sb, memories);
        appendSuggestedActionContext(sb, suggestedAction);
        appendUserIntentContext(sb, userIntent);
        appendResponseInstructions(sb);
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

    private void appendConversationPreferences(
            StringBuilder sb,
            ChatPersonalizationContext personalization) {
        if (personalization == null) {
            return;
        }

        sb.append("\n\n=== PREFERENCIAS CONVERSACIONALES DEL USUARIO ===");
        appendTrustedValue(sb, "Nombre real registrado", personalization.registeredName());
        appendTrustedValue(sb, "Nombre preferido", personalization.preferredName());
        if (personalization.communicationStyle() != null) {
            sb.append("\nEstilo preferido: ")
                    .append(personalization.communicationStyle().displayName());
            sb.append("\nInstrucción de estilo: ")
                    .append(personalization.communicationStyle().promptInstruction());
        }
        sb.append("\nEstas preferencias provienen de datos estructurados del sistema.");
        sb.append("\nUsá el nombre preferido para dirigirte al usuario cuando resulte natural; si no existe, usá el nombre registrado.");
        sb.append("\nRespetá siempre el estilo preferido, sin perder empatía, seguridad ni claridad.");
        sb.append("\nSi el usuario pide explícitamente cambiar su nombre o el estilo, reconocé su pedido y actuá de acuerdo con la preferencia más reciente.");
    }

    private void appendTrustedValue(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String safeValue = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        sb.append("\n").append(label).append(": ").append(safeValue);
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
        sb.append("\n- Incluilo cuando se cumpla alguna de estas condiciones:");
        sb.append("\n  a) El usuario enfrenta una dificultad concreta o bloqueo (no sabe cómo arrancar, está postergando algo, se siente estancado).");
        sb.append("\n  b) El usuario expresó una emoción negativa de intensidad media-alta (>= 5).");
        sb.append("\n  c) El usuario expresa un deseo o aspiración concreto que se puede convertir en una acción pequeña y posible hoy (quiere conocer gente, retomar algo, probar algo nuevo).");
        sb.append("\n- El reto debe ser específico, alcanzable, relacionado con lo que el usuario está viviendo, y sin presión.");
        sb.append("\n- Si no se cumple ninguna condición (conversación casual, saludo, pregunta informativa), devolvé null.");
        sb.append("\n- Si generás un reto, presentalo de forma breve y natural dentro de huly_reply.");
        sb.append("\n- Si hay una ACTIVIDAD RECOMENDADA POR EL SISTEMA, no generes reto: devolvé generated_challenge null.");
        sb.append("\n- Formato cuando corresponde: { \"title\": \"<título corto>\", \"description\": \"<descripción accionable en 1-2 oraciones>\" }");
    }

    private void appendSuggestedActionContext(StringBuilder sb, SuggestedChatAction action) {
        if (action == null) {
            return;
        }

        sb.append("\n\n=== ACTIVIDAD RECOMENDADA POR EL SISTEMA ===");
        sb.append("\nEl sistema ya decidió recomendar esta actividad. Integrala en huly_reply con naturalidad, empatía y brevedad.");
        sb.append("\nNo digas que es una solución garantizada ni prometas resultados.");
        sb.append("\nNo inventes otra actividad ni generes un reto adicional.");
        sb.append("\nActividad: ").append(nullToEmpty(action.title()));
        sb.append("\nTipo: ").append(action.type() != null ? action.type().name() : "");
        sb.append("\nDescripción: ").append(nullToEmpty(action.description()));
    }

    private void appendUserIntentContext(StringBuilder sb, ChatUserIntent userIntent) {
        if (userIntent != ChatUserIntent.CHALLENGE_REQUEST) {
            return;
        }

        sb.append("\n\n=== RETO SOLICITADO POR EL USUARIO ===");
        sb.append("\nEl usuario pidio explicitamente un reto o desafio.");
        sb.append("\nDebes devolver generated_challenge con title y description, salvo que exista una actividad recomendada por el sistema.");
        sb.append("\nEl reto debe ser concreto, pequeno, realizable hoy y coherente con el contexto del usuario.");
        sb.append("\nTambien debes presentar ese reto de forma breve y natural dentro de huly_reply.");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void appendEmotionalAnalysisInstructions(StringBuilder sb) {
        sb.append("\n\n=== ANALISIS EMOCIONAL ESTRUCTURADO ===");
        sb.append("\nAnaliza el mensaje actual usando tambien la informacion recordada del usuario y el historial conversacional disponible.");
        sb.append("\nTu tarea NO es responder al usuario. Tu tarea es producir un analisis emocional estructurado para decidir si conviene recomendar una actividad de bienestar.");
        sb.append("\nNo todos los mensajes requieren recomendacion. Mensajes casuales, saludos, agradecimientos o informacion neutra no deben recomendar actividad.");
        sb.append("\nRecomenda actividad solo si hay senales claras de malestar, ansiedad, tristeza, estres, sobrepensamiento, duelo, bloqueo emocional, baja motivacion o necesidad de regulacion.");
        sb.append("\nCRITICO: shouldRecommend debe ser FALSE cuando el usuario esta motivado, positivo, aspirando a algo o expresando un deseo constructivo, aunque 'podria beneficiarse' de una actividad. La recomendacion es para malestar activo, no para estados positivos. Si valence > 0 y dominance > 0, shouldRecommend debe ser false salvo que haya ademas una senal clara de malestar.");
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
