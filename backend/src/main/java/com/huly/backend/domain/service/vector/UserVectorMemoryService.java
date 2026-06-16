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
import com.huly.backend.domain.provider.VectorMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        saveSimpleMemory(
                userId,
                VectorMemorySource.GUIDED_CLOUDS,
                cloudSessionId,
                GUIDED_CLOUD_INPUT,
                "GUIDED_CLOUD_INPUT",
                content,
                null,
                null,
                "GUIDED_CLOUDS");
    }

    public void rememberJournalEntry(Long userId, Long journalEntryId, String content) {
        String sourceId = journalEntryId != null ? journalEntryId.toString() : null;
        saveSimpleMemory(
                userId,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                sourceId,
                EMOTIONAL_JOURNAL_ENTRY,
                "JOURNAL_ENTRY",
                content,
                null,
                sourceId,
                "EMOTIONAL_JOURNAL");
    }

    public void rememberOnboardingGoals(Long userId, String answer1, String answer2, String answer3) {
        String content = String.format("Goal 1: %s\nGoal 2: %s\nGoal 3: %s", answer1, answer2, answer3);
        saveSimpleMemory(
                userId,
                VectorMemorySource.ONBOARDING,
                userMemorySourceId(userId),
                "ONBOARDING_GOALS",
                "ONBOARDING_GOALS",
                content,
                null,
                null,
                "ONBOARDING");
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
        } catch (Exception e) {
            log.warn("No se pudo guardar memoria vectorial userId={} sourceType={}",
                    command != null ? command.userId() : null,
                    command != null ? command.sourceType() : null,
                    e);
        }
    }

    private void saveSimpleMemory(
            Long userId,
            VectorMemorySource sourceType,
            String sourceId,
            String memoryType,
            String contentType,
            String content,
            String conversationId,
            String relatedEntityId,
            String feature) {
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                sourceType,
                sourceId,
                memoryType,
                contentType,
                content,
                conversationId,
                relatedEntityId,
                metadata(feature)
        ));
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
