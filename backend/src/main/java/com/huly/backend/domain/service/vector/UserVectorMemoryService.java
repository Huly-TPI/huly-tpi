package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.EmotionalEvent;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.RecommendationDecision;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand;
import com.huly.backend.domain.provider.VectorMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.concurrent.CompletableFuture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVectorMemoryService {

    private static final List<VectorMemorySource> ALL_USER_MEMORY_SOURCES = List.of(
            VectorMemorySource.CHATBOT,
            VectorMemorySource.GUIDED_CLOUDS,
            VectorMemorySource.EMOTIONAL_JOURNAL,
            VectorMemorySource.ONBOARDING
    );

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String USER_CHAT_MESSAGE = "USER_CHAT_MESSAGE";
    private static final String GUIDED_CLOUD_INPUT = "GUIDED_CLOUD_INPUT";
    private static final String EMOTIONAL_JOURNAL_ENTRY = "EMOTIONAL_JOURNAL_ENTRY";
    private static final String USER_PROFILE_FACTS = "USER_PROFILE_FACTS";
    private static final String RECOMMENDED_ACTIVITY = "RECOMMENDED_ACTIVITY";
    private static final String ACTIVITY_RECOMMENDATION_DECISION = "ACTIVITY_RECOMMENDATION_DECISION";
    private static final String GENERATED_CHALLENGE = "GENERATED_CHALLENGE";
    private static final String CHALLENGE_DECISION = "CHALLENGE_DECISION";

    private final VectorMemoryService vectorMemoryService;
    private final VectorMemoryProperties vectorMemoryProperties;
    private final UserProfileFactExtractor userProfileFactExtractor;
    private final ObjectProvider<ChatModel> chatModelProvider;
    private final JdbcTemplate jdbcTemplate;

    public List<VectorMemory> findRelevantUserMemories(Long userId, String query) {
        return findRelevantUserMemoriesBySources(userId, ALL_USER_MEMORY_SOURCES, query);
    }

    public List<VectorMemory> findRelevantUserMemories(Long userId, VectorMemorySource sourceType, String query) {
        try {
            List<VectorMemory> memories = new ArrayList<>();
            for (String recallQuery : buildRecallQueries(query)) {
                memories.addAll(vectorMemoryService.findRelevantMemories(new SearchVectorMemoryQuery(
                        userId,
                        sourceType,
                        recallQuery,
                        recallLimit(query),
                        recallThreshold(query)
                )));
            }
            List<VectorMemory> result = uniqueRankedAndLimited(memories);
            log.debug("Memorias vectoriales recuperadas userId={} sourceType={} limit={} threshold={} count={}",
                    userId,
                    sourceType,
                    recallLimit(query),
                    recallThreshold(query),
                    result.size());
            return result;
        } catch (Exception e) {
            log.warn("No se pudo recuperar memoria vectorial userId={} sourceType={}", userId, sourceType, e);
            return List.of();
        }
    }

    public List<VectorMemory> findRelevantUserMemoriesBySources(
            Long userId,
            List<VectorMemorySource> sourceTypes,
            String query
    ) {
        try {
            List<VectorMemory> memories = new ArrayList<>();
            for (String recallQuery : buildRecallQueries(query)) {
                memories.addAll(vectorMemoryService.findRelevantMemories(new SearchVectorMemoriesQuery(
                        userId,
                        sourceTypes,
                        recallQuery,
                        recallLimit(query),
                        recallThreshold(query)
                )));
            }
            List<VectorMemory> result = uniqueRankedAndLimited(memories);
            log.debug("Memorias vectoriales recuperadas userId={} sourceTypes={} limit={} threshold={} count={}",
                    userId,
                    sourceTypes,
                    recallLimit(query),
                    recallThreshold(query),
                    result.size());
            return result;
        } catch (Exception e) {
            log.warn("No se pudo recuperar memoria vectorial multi-source userId={} sourceTypes={}",
                    userId, sourceTypes, e);
            return List.of();
        }
    }

    public void rememberChatMessage(Long userId, String conversationId, String message) {
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                userMemorySourceId(userId),
                USER_CHAT_MESSAGE,
                "CHAT_MESSAGE",
                message,
                conversationId,
                null,
                metadata("CHATBOT")
        ));

        userProfileFactExtractor.extractProfileFacts(message)
                .ifPresent(profileFacts -> saveMemory(new SaveVectorMemoryCommand(
                        userId,
                        VectorMemorySource.CHATBOT,
                        userMemorySourceId(userId),
                        USER_PROFILE_FACTS,
                        "PROFILE_FACTS",
                        profileFacts,
                        conversationId,
                        null,
                metadata("CHATBOT_PROFILE")
                )));
    }

    public void rememberRecommendedActivity(
            Long userId,
            String conversationId,
            Long emotionalEventId,
            SuggestedChatAction action
    ) {
        if (action == null) {
            return;
        }

        String content = "Huly recomendo la actividad: %s. Tipo: %s. Descripcion: %s."
                .formatted(
                        valueOrDefault(action.title(), "Actividad"),
                        action.type() != null ? action.type().name() : "UNKNOWN",
                        valueOrDefault(action.description(), "")
                );
        Map<String, Object> metadata = metadata("CHATBOT_ACTIVITY_RECOMMENDATION",
                Map.of(
                        "activityId", valueOrDefault(action.activityId(), ""),
                        "activityType", action.type() != null ? action.type().name() : "",
                        "emotionalEventId", valueOrDefault(emotionalEventId, "")
                ));

        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                emotionalEventId != null ? emotionalEventId.toString() : userMemorySourceId(userId),
                RECOMMENDED_ACTIVITY,
                "RECOMMENDED_ACTIVITY",
                content,
                conversationId,
                emotionalEventId != null ? emotionalEventId.toString() : null,
                metadata
        ));
    }

    public void rememberActivityRecommendationDecision(EmotionalEvent event) {
        if (event == null || event.getUserId() == null || event.getRecommendationDecision() == null) {
            return;
        }

        RecommendationDecision decision = event.getRecommendationDecision();
        String content = "El usuario %s la recomendacion de actividad. Actividad recomendada id: %s. Actividad elegida id: %s. Recomendacion: %s."
                .formatted(
                        activityDecisionText(decision),
                        valueOrDefault(event.getRecommendedActivityId(), ""),
                        valueOrDefault(event.getChosenActivityId(), ""),
                        valueOrDefault(event.getGeneratedRecommendation(), "")
                );

        saveMemory(new SaveVectorMemoryCommand(
                event.getUserId(),
                VectorMemorySource.CHATBOT,
                event.getId() != null ? event.getId().toString() : userMemorySourceId(event.getUserId()),
                ACTIVITY_RECOMMENDATION_DECISION,
                "ACTIVITY_RECOMMENDATION_DECISION",
                content,
                null,
                event.getId() != null ? event.getId().toString() : null,
                metadata("CHATBOT_ACTIVITY_DECISION",
                        Map.of(
                                "decision", decision.name(),
                                "recommendedActivityId", valueOrDefault(event.getRecommendedActivityId(), ""),
                                "chosenActivityId", valueOrDefault(event.getChosenActivityId(), ""),
                                "emotionalEventId", valueOrDefault(event.getId(), "")
                        ))
        ));
    }

    public void rememberGeneratedChallenge(Long userId, String conversationId, ChatReply.GeneratedChallenge challenge) {
        if (challenge == null || isBlank(challenge.title())) {
            return;
        }

        String content = "Huly sugirio el reto: %s. Descripcion: %s."
                .formatted(challenge.title(), valueOrDefault(challenge.description(), ""));

        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                sourceId("generated-challenge", conversationId, challenge.title()),
                GENERATED_CHALLENGE,
                "GENERATED_CHALLENGE",
                content,
                conversationId,
                null,
                metadata("CHATBOT_CHALLENGE",
                        Map.of(
                                "challengeTitle", challenge.title(),
                                "challengeDescription", valueOrDefault(challenge.description(), "")
                        ))
        ));
    }

    public void rememberChallengeDecision(
            Long userId,
            String conversationId,
            String title,
            String description,
            String decision
    ) {
        if (userId == null || isBlank(title) || isBlank(decision)) {
            return;
        }

        String normalizedDecision = decision.toUpperCase();
        String content = "El usuario %s el reto: %s. Descripcion: %s."
                .formatted(challengeDecisionText(normalizedDecision), title, valueOrDefault(description, ""));

        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                sourceId("challenge-decision", conversationId, title, normalizedDecision),
                CHALLENGE_DECISION,
                "CHALLENGE_DECISION",
                content,
                conversationId,
                null,
                metadata("CHATBOT_CHALLENGE_DECISION",
                        Map.of(
                                "decision", normalizedDecision,
                                "challengeTitle", title,
                                "challengeDescription", valueOrDefault(description, "")
                        ))
        ));
    }

    public void rememberGuidedCloudInput(Long userId, String cloudSessionId, String content) {
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.GUIDED_CLOUDS,
                cloudSessionId,
                GUIDED_CLOUD_INPUT,
                "GUIDED_CLOUD_INPUT",
                content,
                null,
                null,
                metadata("GUIDED_CLOUDS")
        ));
    }

    public void rememberJournalEntry(Long userId, Long journalEntryId, String content) {
        String sourceId = journalEntryId != null ? journalEntryId.toString() : null;
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                sourceId,
                EMOTIONAL_JOURNAL_ENTRY,
                "JOURNAL_ENTRY",
                content,
                null,
                sourceId,
                metadata("EMOTIONAL_JOURNAL")
        ));
    }

    public void rememberOnboardingGoals(Long userId, String answer1, String answer2, String answer3) {
        String content = String.format("Goal 1: %s\nGoal 2: %s\nGoal 3: %s", answer1, answer2, answer3);
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.ONBOARDING,
                userMemorySourceId(userId),
                "ONBOARDING_GOALS",
                "ONBOARDING_GOALS",
                content,
                null,
                null,
                metadata("ONBOARDING")
        ));
    }

    private List<String> buildRecallQueries(String query) {
        if (!Boolean.TRUE.equals(userProfileFactExtractor.asksForProfileFact(query))) {
            return List.of(query);
        }

        String profileRecallQuery = userProfileFactExtractor.buildProfileRecallQuery(query);
        if (profileRecallQuery.equals(query)) {
            return List.of(query);
        }
        return List.of(query, profileRecallQuery);
    }

    private Integer recallLimit(String query) {
        Integer defaultLimit = vectorMemoryProperties.getDefaultLimit();
        if (!Boolean.TRUE.equals(userProfileFactExtractor.asksForProfileFact(query))) {
            return defaultLimit;
        }

        Integer maxLimit = vectorMemoryProperties.getMaxLimit();
        if (maxLimit == null) {
            return defaultLimit;
        }
        return Math.min(maxLimit, Math.max(defaultLimit, 10));
    }

    private Double recallThreshold(String query) {
        if (Boolean.TRUE.equals(userProfileFactExtractor.asksForProfileFact(query))) {
            return 0.0d;
        }
        return vectorMemoryProperties.getRecallSimilarityThreshold();
    }

    private List<VectorMemory> uniqueRankedAndLimited(List<VectorMemory> memories) {
        Map<String, VectorMemory> unique = new LinkedHashMap<>();
        for (VectorMemory memory : memories) {
            if (memory == null) {
                continue;
            }
            String key = memory.id() != null ? memory.id() : memory.sourceType() + ":" + memory.content();
            VectorMemory previous = unique.get(key);
            if (previous == null || score(memory) > score(previous)) {
                unique.put(key, memory);
            }
        }

        return unique.values().stream()
                .sorted(Comparator.comparing(this::score).reversed())
                .limit(vectorMemoryProperties.getDefaultLimit())
                .toList();
    }

    private Double score(VectorMemory memory) {
        return memory.score() != null ? memory.score() : 0.0d;
    }

    private void saveMemory(SaveVectorMemoryCommand command) {
        try {
            vectorMemoryService.saveMemory(command);
            if (command != null && command.userId() != null && !"PERSONALITY_SUMMARY".equals(command.contentType())) {
                CompletableFuture.runAsync(() -> {
                    generateAndSavePersonalitySummary(command.userId());
                });
            }
        } catch (Exception e) {
            log.warn("No se pudo guardar memoria vectorial userId={} sourceType={}",
                    command != null ? command.userId() : null,
                    command != null ? command.sourceType() : null,
                    e);
        }
    }

    public void deletePersonalitySummary(Long userId) {
        try {
            vectorMemoryService.deleteMemories(new DeleteVectorMemoryCommand(
                    userId,
                    VectorMemorySource.CHATBOT,
                    "personality-summary"
            ));
        } catch (Exception e) {
            log.warn("Error deleting old personality summary: {}", e.getMessage());
        }
    }

    public List<String> getAllMemoryContents(Long userId) {
        try {
            String sql = "SELECT content FROM vector_store WHERE metadata ->> 'userId' = ? AND COALESCE(metadata ->> 'deleted', 'false') = 'false' AND (metadata ->> 'contentType' IS NULL OR metadata ->> 'contentType' != 'PERSONALITY_SUMMARY')";
            return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("content"), userId.toString());
        } catch (Exception e) {
            log.warn("Error getting all memory contents for userId={}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    private void generateAndSavePersonalitySummary(Long userId) {
        try {
            List<String> contents = getAllMemoryContents(userId);
            if (contents.isEmpty()) {
                log.info("No hay memorias suficientes para generar resumen de personalidad para userId={}", userId);
                return;
            }

            String memoriesJoined = String.join("\n- ", contents);
            if (memoriesJoined.length() > 4000) {
                memoriesJoined = memoriesJoined.substring(0, 4000) + "...";
            }

            String systemPrompt = """
                Eres un psicólogo clínico experto analizando el comportamiento de un usuario en base a sus registros de interacción con un asistente de bienestar.
                Tu tarea es generar un análisis del perfil psicológico/conductual y receptividades del usuario en formato JSON.
                
                Debes responder estrictamente con un objeto JSON válido estructurado con las siguientes claves (no agregues introducciones, explicaciones ni formato adicional, solo el JSON puro):
                {
                  "summary": "Un párrafo corto (de 3 a 4 oraciones como máximo) en español sobre el perfil psicológico y conductual del usuario. Sé empático, profesional y constructivo. IMPORTANTE: No comiences el texto con títulos, negritas ni asteriscos (no uses '**Perfil Psicológico y Conductual**' ni similar). Tampoco menciones términos técnicos (como 'logs', 'memoria', 'vectores', etc.).",
                  "accepted": "Una frase muy corta y concisa de 3 a 5 palabras en español que generalice lo que el usuario suele aceptar (ej. 'actividades relajantes', 'retos sencillos al aire libre').",
                  "rejected": "Una frase muy corta y concisa de 3 a 5 palabras en español que generalice lo que el usuario suele rechazar o ignorar (ej. 'ejercicios físicos exigentes', 'interacciones sociales')."
                }
                """;

            String userMessage = "Analiza las siguientes memorias del usuario para estructurar el JSON:\n\n- " + memoriesJoined;

            ChatModel chat = chatModelProvider.getIfAvailable();
            if (chat == null) {
                log.warn("ChatModel no disponible. No se puede generar perfil de personalidad.");
                return;
            }

            org.springframework.ai.chat.model.ChatResponse response = chat.call(new org.springframework.ai.chat.prompt.Prompt(List.of(
                new org.springframework.ai.chat.messages.SystemMessage(systemPrompt),
                new org.springframework.ai.chat.messages.UserMessage(userMessage)
            )));

            if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
                String summary = response.getResult().getOutput().getText();
                if (summary != null && !summary.isBlank()) {
                    deletePersonalitySummary(userId);

                    saveMemory(new SaveVectorMemoryCommand(
                            userId,
                            VectorMemorySource.CHATBOT,
                            "personality-summary",
                            "PERSONALITY_SUMMARY",
                            "PERSONALITY_SUMMARY",
                            summary.trim(),
                            null,
                            null,
                            Map.of("contentType", "PERSONALITY_SUMMARY", "feature", "PERSONALITY_SUMMARY")
                    ));
                    log.info("Perfil de personalidad generado e insertado para userId={}", userId);
                }
            }
        } catch (Exception e) {
            log.warn("Error generando perfil de personalidad para userId={}", userId, e);
        }
    }

    private String userMemorySourceId(Long userId) {
        return userId != null ? userId.toString() : null;
    }

    private Map<String, Object> metadata(String feature) {
        return Map.of(
                "createdFrom", CREATED_FROM_USER_MESSAGE,
                "feature", feature
        );
    }

    private Map<String, Object> metadata(String feature, Map<String, Object> extra) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdFrom", CREATED_FROM_USER_MESSAGE);
        metadata.put("feature", feature);
        if (extra != null) {
            metadata.putAll(extra);
        }
        return metadata;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String valueOrDefault(Object value, String defaultValue) {
        return value == null ? defaultValue : value.toString();
    }

    private String activityDecisionText(RecommendationDecision decision) {
        if (decision == RecommendationDecision.ACCEPTED) {
            return "acepto";
        }
        if (decision == RecommendationDecision.IGNORED) {
            return "rechazo";
        }
        return "eligio otra actividad para";
    }

    private String challengeDecisionText(String decision) {
        return "ACCEPTED".equals(decision) ? "acepto" : "rechazo";
    }

    private String sourceId(String... parts) {
        return String.join(":", java.util.Arrays.stream(parts)
                .map(part -> part == null || part.isBlank() ? "unknown" : part.strip())
                .toList());
    }
}
