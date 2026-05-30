package com.huly.backend.domain.service.vector;

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
            VectorMemorySource.EMOTIONAL_JOURNAL
    );

    private static final String CREATED_FROM_USER_MESSAGE = "USER_MESSAGE";
    private static final String USER_CHAT_MESSAGE = "USER_CHAT_MESSAGE";
    private static final String GUIDED_CLOUD_INPUT = "GUIDED_CLOUD_INPUT";
    private static final String EMOTIONAL_JOURNAL_ENTRY = "EMOTIONAL_JOURNAL_ENTRY";
    private static final String USER_PROFILE_FACTS = "USER_PROFILE_FACTS";

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

    private String userMemorySourceId(Long userId) {
        return userId != null ? userId.toString() : null;
    }

    private Map<String, Object> metadata(String feature) {
        return Map.of(
                "createdFrom", CREATED_FROM_USER_MESSAGE,
                "feature", feature
        );
    }
}
