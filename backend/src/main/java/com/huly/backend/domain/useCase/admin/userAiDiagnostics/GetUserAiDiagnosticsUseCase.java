package com.huly.backend.domain.useCase.admin.userAiDiagnostics;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.UserPersonalitySummary;
import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.repository.EmotionalEventRepository;
import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.domain.repository.VectorMemoryRepository;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class GetUserAiDiagnosticsUseCase {

    private final UserRepository userRepository;
    private final EmotionalEventRepository emotionalEventRepository;
    private final VectorMemoryRepository vectorMemoryRepository;
    private final UserPersonalitySummaryRepository userPersonalitySummaryRepository;
    private final ChatConversationPreferenceRepository chatConversationPreferenceRepository;

    public GetUserAiDiagnosticsResponse execute(GetUserAiDiagnosticsRequest request) {
        Long userId = request.userId();

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<VectorMemoryEntry> memories = vectorMemoryRepository.findMemoriesByUserIdExcludingSummary(user.getId());
        List<VectorMemoryResponse> aiMemories = memories.stream()
                .map(memory -> new VectorMemoryResponse(
                        memory.id(),
                        memory.content(),
                        memory.sourceType(),
                        memory.contentType(),
                        memory.createdAt()
                ))
                .toList();

        List<EmotionalEventResponse> emotionalEvents = emotionalEventRepository.findByUserId(userId).stream()
                .map(this::toEmotionalEventResponse)
                .toList();

        String preferredName = chatConversationPreferenceRepository.findByUserId(userId)
                .map(ChatConversationPreference::getPreferredName)
                .orElse("No especificado");
        String communicationStyle = chatConversationPreferenceRepository.findByUserId(userId)
                .map(ChatConversationPreference::getCommunicationStyle)
                .map(style -> style.displayName())
                .orElse("Neutro");

        List<EmotionalEventResponse> recommendationEvents = emotionalEventRepository.findRecommendationEventsByUserId(userId).stream()
                .map(this::toEmotionalEventResponse)
                .toList();

        int acceptedCount = 0;
        Set<String> acceptedActivities = new LinkedHashSet<>();
        Set<String> ignoredActivities = new LinkedHashSet<>();
        for (EmotionalEventResponse event : recommendationEvents) {
            if ("ACCEPTED".equals(event.recommendationDecision())) {
                acceptedCount++;
            }

            if (event.generatedRecommendation() == null || event.generatedRecommendation().isBlank()) {
                continue;
            }

            String simplifiedRecommendationName = simplifyRecommendationName(event.generatedRecommendation());

            if ("ACCEPTED".equals(event.recommendationDecision())) {
                acceptedActivities.add(simplifiedRecommendationName);
            } else {
                ignoredActivities.add(simplifiedRecommendationName);
            }
        }

        int totalRecommendations = recommendationEvents.size();
        for (VectorMemoryEntry memory : memories) {
            if (memory.content() == null) {
                continue;
            }

            if (!"CHALLENGE_DECISION".equals(memory.sourceType()) && !"CHALLENGE_DECISION".equals(memory.contentType())) {
                continue;
            }

            String content = memory.content().toLowerCase();
            boolean accepted = content.contains("reto aceptado")
                    || content.contains("acepto el reto")
                    || content.contains("aceptó el reto");
            boolean rejected = content.contains("reto rechazado")
                    || content.contains("rechazo el reto")
                    || content.contains("rechazó el reto");
            if (!accepted && !rejected) {
                continue;
            }

            totalRecommendations++;
            if (accepted) {
                acceptedActivities.add("Retos Diarios");
                acceptedCount++;
            } else {
                ignoredActivities.add("Retos Diarios");
            }
        }

        int receptivityScore = totalRecommendations > 0
                ? (int) Math.round(((double) acceptedCount / totalRecommendations) * 100)
                : 0;
        String receptivityLabel = "Sin recomendaciones registradas";
        if (totalRecommendations > 0) {
            if (receptivityScore >= 75) {
                receptivityLabel = "Alta receptividad";
            } else if (receptivityScore >= 40) {
                receptivityLabel = "Receptividad moderada";
            } else {
                receptivityLabel = "Baja receptividad";
            }
        }

        StringBuilder memoryContent = new StringBuilder();
        for (VectorMemoryEntry memory : memories) {
            if (memory.content() != null) {
                memoryContent.append(memory.content().toLowerCase()).append(" ");
            }
        }

        String normalizedContent = memoryContent.toString();
        List<String> topicsDetected = detectTopics(normalizedContent);
        List<String> copingStrategies = detectCopingStrategies(normalizedContent);

        Map<String, Integer> emotionDistribution = new LinkedHashMap<>();
        for (EmotionalEventResponse event : emotionalEvents) {
            if (event.detectedEmotion() == null) {
                continue;
            }

            String emotion = event.detectedEmotion().trim().toUpperCase();
            emotionDistribution.put(emotion, emotionDistribution.getOrDefault(emotion, 0) + 1);
        }

        String dominantEmotion = "NEUTRAL";
        int maxEmotionCount = 0;
        for (Map.Entry<String, Integer> entry : emotionDistribution.entrySet()) {
            if (entry.getValue() > maxEmotionCount) {
                maxEmotionCount = entry.getValue();
                dominantEmotion = entry.getKey();
            }
        }

        UserPersonalitySummary personalitySummary = userPersonalitySummaryRepository.findByUserId(userId)
                .orElse(null);

        String finalSummary = personalitySummary != null && personalitySummary.getSummary() != null
                ? personalitySummary.getSummary()
                : "No tiene memorias suficientes para generar una síntesis de IA.";
        List<String> finalAccepted = acceptedActivities.stream().limit(3).toList();
        List<String> finalIgnored = ignoredActivities.stream().limit(3).toList();

        if (personalitySummary != null) {
            if (personalitySummary.getAccepted() != null && !personalitySummary.getAccepted().isBlank()) {
                finalAccepted = List.of(personalitySummary.getAccepted());
            }
            if (personalitySummary.getRejected() != null && !personalitySummary.getRejected().isBlank()) {
                finalIgnored = List.of(personalitySummary.getRejected());
            }
        }

        if (finalSummary != null) {
            finalSummary = finalSummary
                    .replaceAll("(?i)^\\*\\*Perfil Psicológico y Conductual\\*\\*\\s*", "")
                    .replaceAll("(?i)^Perfil Psicológico y Conductual:\\s*", "")
                    .replace("**", "")
                    .trim();
        }

        return new GetUserAiDiagnosticsResponse(
                aiMemories,
                emotionalEvents,
                preferredName,
                communicationStyle,
                finalSummary,
                topicsDetected,
                copingStrategies,
                receptivityScore,
                receptivityLabel,
                finalAccepted,
                finalIgnored,
                dominantEmotion,
                emotionDistribution
        );
    }

    private EmotionalEventResponse toEmotionalEventResponse(EmotionalEvent event) {
        return new EmotionalEventResponse(
                event.getId(),
                Objects.requireNonNull(event.getSource(), "EmotionalEvent source is required").name(),
                event.getInputText(),
                event.getDetectedEmotion(),
                event.getConfidence(),
                event.getValence(),
                event.getArousal(),
                event.getDominance(),
                event.getIntensity(),
                event.getUserGoal(),
                event.getGeneratedRecommendation(),
                event.getRecommendedActivityId(),
                event.getChosenActivityId(),
                event.getRecommendationDecision() != null ? event.getRecommendationDecision().name() : null,
                event.getFeedbackScore(),
                event.getFeedbackText(),
                event.getCreatedAt()
        );
    }

    private List<String> detectTopics(String content) {
        Map<String, List<String>> topics = new LinkedHashMap<>();
        topics.put("Estrés laboral o académico", List.of("trabajo", "laboral", "jefe", "oficina", "empleo", "estudio", "examen", "universidad", "facultad", "tarea"));
        topics.put("Ansiedad o Preocupaciones", List.of("ansiedad", "ansioso", "estrés", "estresado", "nervioso", "preocupado", "miedo", "angustia"));
        topics.put("Descanso e Insomnio", List.of("dormir", "sueño", "insomnio", "descanso", "cansado", "noche", "pesadilla"));
        topics.put("Relaciones y Vínculos", List.of("familia", "amigos", "pareja", "novio", "novia", "hijo", "hija", "padre", "madre", "amigo", "amiga"));
        topics.put("Ánimo decaído o Tristeza", List.of("triste", "tristeza", "desanimado", "bajón", "llorar", "solo", "soledad"));

        List<String> detectedTopics = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : topics.entrySet()) {
            if (entry.getValue().stream().anyMatch(content::contains)) {
                detectedTopics.add(entry.getKey());
            }
        }
        return detectedTopics;
    }

    private List<String> detectCopingStrategies(String content) {
        Map<String, List<String>> strategies = new LinkedHashMap<>();
        strategies.put("Música y Arte", List.of("música", "canción", "escuchar", "cantar", "tocar", "dibujar", "pintar"));
        strategies.put("Actividad Física", List.of("ejercicio", "deporte", "correr", "gimnasio", "caminar", "entrenar", "yoga"));
        strategies.put("Meditación y Respiración", List.of("meditar", "respirar", "respiración", "relajar", "mindfulness", "pausa"));
        strategies.put("Escritura Reflexiva", List.of("escribir", "diario", "anotar", "pensamiento"));

        List<String> detectedStrategies = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : strategies.entrySet()) {
            if (entry.getValue().stream().anyMatch(content::contains)) {
                detectedStrategies.add(entry.getKey());
            }
        }
        return detectedStrategies;
    }

    private String simplifyRecommendationName(String name) {
        String normalizedName = name.toLowerCase();
        if (normalizedName.contains("respiracion") || normalizedName.contains("respiración") || normalizedName.contains("breathing")) {
            return "Respiración Guiada";
        }

        if (normalizedName.contains("diario") || normalizedName.contains("journal")) {
            return "Diario Emocional";
        }

        if (normalizedName.contains("nube") || normalizedName.contains("cloud")) {
            return "Nubes de Pensamiento";
        }

        if (normalizedName.contains("burbuja") || normalizedName.contains("bubble")) {
            return "Reventar Burbujas";
        }

        if (normalizedName.contains("reto") || normalizedName.contains("challenge")) {
            return "Retos Diarios";
        }

        return name;
    }
}
