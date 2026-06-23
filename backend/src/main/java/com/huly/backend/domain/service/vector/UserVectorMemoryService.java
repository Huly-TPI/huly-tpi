package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.user.UserPersonalitySummary;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.port.VectorMemoryPort;
import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserVectorMemoryService {

    private static final List<VectorMemorySource> ALL_USER_MEMORY_SOURCES = List.of(
            VectorMemorySource.CHATBOT,
            VectorMemorySource.GUIDED_LANTERNS,
            VectorMemorySource.EMOTIONAL_JOURNAL,
            VectorMemorySource.ONBOARDING
    );

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String USER_CHAT_MESSAGE = "USER_CHAT_MESSAGE";
    private static final String GUIDED_LANTERN_INPUT = "GUIDED_LANTERN_INPUT";
    private static final String EMOTIONAL_JOURNAL_ENTRY = "EMOTIONAL_JOURNAL_ENTRY";
    private static final String USER_PROFILE_FACTS = "USER_PROFILE_FACTS";
    private static final String RECOMMENDED_ACTIVITY = "RECOMMENDED_ACTIVITY";
    private static final String ACTIVITY_RECOMMENDATION_DECISION = "ACTIVITY_RECOMMENDATION_DECISION";
    private static final String GENERATED_CHALLENGE = "GENERATED_CHALLENGE";
    private static final String CHALLENGE_DECISION = "CHALLENGE_DECISION";

    record PersonalitySummaryDto(String summary, String accepted, String rejected) {}

    private final VectorMemoryPort vectorMemoryPort;
    private final VectorMemoryProperties vectorMemoryProperties;
    private final UserProfileFactExtractor userProfileFactExtractor;
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final UserPersonalitySummaryRepository userPersonalitySummaryRepository;
    private final org.springframework.core.io.Resource personalitySummaryPrompt;

    public UserVectorMemoryService(
            VectorMemoryPort vectorMemoryPort,
            VectorMemoryProperties vectorMemoryProperties,
            UserProfileFactExtractor userProfileFactExtractor,
            ObjectProvider<ChatClient> chatClientProvider,
            UserPersonalitySummaryRepository userPersonalitySummaryRepository,
            @Value("classpath:/prompts/personality-summary.st") org.springframework.core.io.Resource personalitySummaryPrompt) {
        this.vectorMemoryPort = vectorMemoryPort;
        this.vectorMemoryProperties = vectorMemoryProperties;
        this.userProfileFactExtractor = userProfileFactExtractor;
        this.chatClientProvider = chatClientProvider;
        this.userPersonalitySummaryRepository = userPersonalitySummaryRepository;
        this.personalitySummaryPrompt = personalitySummaryPrompt;
    }

    public List<VectorMemory> findRelevantUserMemories(Long userId, String query) {
        return findRelevantUserMemoriesBySources(userId, ALL_USER_MEMORY_SOURCES, query);
    }

    public List<VectorMemory> findRelevantUserMemories(Long userId, VectorMemorySource sourceType, String query) {
        try {
            List<VectorMemory> memories = new ArrayList<>();
            for (String recallQuery : buildRecallQueries(query)) {
                memories.addAll(vectorMemoryPort.findRelevantMemories(new SearchVectorMemoryQuery(
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
                memories.addAll(vectorMemoryPort.findRelevantMemories(new SearchVectorMemoriesQuery(
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

    public void saveMemory(SaveVectorMemoryCommand command) {
        try {
            vectorMemoryPort.saveMemory(command);
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
            userPersonalitySummaryRepository.deleteByUserId(userId);
        } catch (Exception e) {
            log.warn("Error deleting old personality summary: {}", e.getMessage());
        }
    }

    public void rememberChatMessage(Long userId, String conversationId, String message) {
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                userId != null ? userId.toString() : null,
                "USER_CHAT_MESSAGE",
                "CHAT_MESSAGE",
                message,
                conversationId,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "CHATBOT")
        ));
    }

    public void rememberGeneratedChallenge(Long userId, String conversationId, ChatReply.GeneratedChallenge challenge) {
        if (challenge == null || challenge.title() == null || challenge.title().isBlank()) {
            return;
        }
        String content = "Huly sugirio el reto: %s. Descripcion: %s."
                .formatted(challenge.title(), challenge.description() == null ? "" : challenge.description());
        String normalizedConversationId = conversationId != null && !conversationId.isBlank()
                ? conversationId.strip()
                : "unknown";
        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                String.join(":", "generated-challenge", normalizedConversationId, challenge.title().strip()),
                "GENERATED_CHALLENGE",
                "GENERATED_CHALLENGE",
                content,
                conversationId,
                null,
                Map.of(
                        "createdFrom", "USER_MESSAGE",
                        "feature", "CHATBOT_CHALLENGE",
                        "challengeTitle", challenge.title(),
                        "challengeDescription", challenge.description() == null ? "" : challenge.description()
                )
        ));
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
                        action.title() == null ? "Actividad" : action.title(),
                        action.type() != null ? action.type().name() : "UNKNOWN",
                        action.description() == null ? "" : action.description()
                );
        Map<String, Object> extra = new HashMap<>();
        extra.put("createdFrom", "USER_MESSAGE");
        extra.put("feature", "CHATBOT_ACTIVITY_RECOMMENDATION");
        extra.put("activityId", action.activityId() == null ? "" : action.activityId().toString());
        extra.put("activityType", action.type() != null ? action.type().name() : "");
        extra.put("emotionalEventId", emotionalEventId != null ? emotionalEventId.toString() : "");

        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                emotionalEventId != null ? emotionalEventId.toString() : (userId != null ? userId.toString() : null),
                "RECOMMENDED_ACTIVITY",
                "RECOMMENDED_ACTIVITY",
                content,
                conversationId,
                emotionalEventId != null ? emotionalEventId.toString() : null,
                extra
        ));
    }

    public void rememberChallengeDecision(
            Long userId,
            String conversationId,
            String title,
            String description,
            String decision
    ) {
        if (userId == null || title == null || title.isBlank() || decision == null || decision.isBlank()) {
            return;
        }

        String normalizedDecision = decision.toUpperCase();
        String decisionText = "ACCEPTED".equals(normalizedDecision) ? "acepto" : "rechazo";
        String safeDescription = description != null ? description : "";
        String content = "El usuario %s el reto: %s. Descripcion: %s."
                .formatted(decisionText, title, safeDescription);
        String normalizedConversationId = conversationId != null && !conversationId.isBlank()
                ? conversationId.strip()
                : "unknown";
        String sourceId = String.join(":", "challenge-decision", normalizedConversationId, title.strip(), normalizedDecision);

        saveMemory(new SaveVectorMemoryCommand(
                userId,
                VectorMemorySource.CHATBOT,
                sourceId,
                "CHALLENGE_DECISION",
                "CHALLENGE_DECISION",
                content,
                conversationId,
                null,
                Map.of(
                        "createdFrom", "USER_MESSAGE",
                        "feature", "CHATBOT_CHALLENGE_DECISION",
                        "decision", normalizedDecision,
                        "challengeTitle", title,
                        "challengeDescription", safeDescription
                )
        ));
    }

    public List<String> getAllMemoryContents(Long userId) {
        try {
            return vectorMemoryPort.findMemoryContentsByUserIdExcludingSummary(userId);
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

            String userMessage = "Analiza las siguientes memorias del usuario para estructurar el JSON:\n\n- " + memoriesJoined;

            ChatClient chatClient = chatClientProvider.getIfAvailable();
            if (chatClient == null) {
                log.warn("ChatClient no disponible. No se puede generar perfil de personalidad.");
                return;
            }

            PersonalitySummaryDto dto = chatClient.prompt()
                    .system(personalitySummaryPrompt)
                    .user(userMessage)
                    .call()
                    .entity(PersonalitySummaryDto.class);

            if (dto != null && dto.summary() != null && !dto.summary().isBlank()) {
                Instant now = Instant.now();
                userPersonalitySummaryRepository.save(UserPersonalitySummary.builder()
                        .userId(userId)
                        .summary(dto.summary().trim())
                        .accepted(normalizeOptionalValue(dto.accepted()))
                        .rejected(normalizeOptionalValue(dto.rejected()))
                        .generatedAt(now)
                        .updatedAt(now)
                        .build());
                log.info("Perfil de personalidad generado e insertado para userId={}", userId);
            }
        } catch (Exception e) {
            log.warn("Error generando perfil de personalidad para userId={}", userId, e);
        }
    }

    private List<String> buildRecallQueries(String query) {
        if (!Boolean.TRUE.equals(userProfileFactExtractor.asksForProfileFact(query)))
            return List.of(query);


        String profileRecallQuery = userProfileFactExtractor.buildProfileRecallQuery(query);
        if (profileRecallQuery.equals(query))
            return List.of(query);

        return List.of(query, profileRecallQuery);
    }

    private Integer recallLimit(String query) {
        Integer defaultLimit = vectorMemoryProperties.getDefaultLimit();
        if (!Boolean.TRUE.equals(userProfileFactExtractor.asksForProfileFact(query)))
            return defaultLimit;


        Integer maxLimit = vectorMemoryProperties.getMaxLimit();
        if (maxLimit == null) {
            return defaultLimit;
        }
        return Math.min(maxLimit, Math.max(defaultLimit, 10));
    }

    private Double recallThreshold(String query) {
        if (Boolean.TRUE.equals(userProfileFactExtractor.asksForProfileFact(query)))
            return 0.0d;

        return vectorMemoryProperties.getRecallSimilarityThreshold();
    }

    private List<VectorMemory> uniqueRankedAndLimited(List<VectorMemory> memories) {
        Map<String, VectorMemory> unique = new LinkedHashMap<>();
        for (VectorMemory memory : memories) {
            if (memory == null)
                continue;

            String key = memory.id() != null ? memory.id() : memory.sourceType() + ":" + memory.content();
            VectorMemory previous = unique.get(key);
            if (previous == null || score(memory) > score(previous))
                unique.put(key, memory);

        }

        return unique.values().stream()
                .sorted(Comparator.comparing(this::score).reversed())
                .limit(vectorMemoryProperties.getDefaultLimit())
                .toList();
    }

    private Double score(VectorMemory memory) {
        return memory.score() != null ? memory.score() : 0.0d;
    }

    private String normalizeOptionalValue(String value) {
        if (value == null || value.isBlank() || "N/A".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value.trim();
    }
}
