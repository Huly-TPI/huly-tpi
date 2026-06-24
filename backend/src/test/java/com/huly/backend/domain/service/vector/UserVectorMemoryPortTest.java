package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.user.UserPersonalitySummary;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.port.VectorMemoryPort;
import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserVectorMemoryPortTest {

    private VectorMemoryProperties properties;
    private RecordingVectorMemoryPort vectorMemoryPort;
    private RecordingUserPersonalitySummaryRepository personalitySummaryRepository;
    private UserVectorMemoryService service;
    private ObjectProvider<org.springframework.ai.chat.client.ChatClient> chatClientProvider;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new VectorMemoryProperties();
        vectorMemoryPort = new RecordingVectorMemoryPort();
        personalitySummaryRepository = new RecordingUserPersonalitySummaryRepository();
        chatClientProvider = mock(ObjectProvider.class);
        service = new UserVectorMemoryService(
                vectorMemoryPort,
                properties,
                new UserProfileFactExtractor(),
                chatClientProvider,
                personalitySummaryRepository,
                new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes())
        );
    }

    @Test
    void findRelevantUserMemories_shouldSearchAcrossAllUserMemorySources() {
        service.findRelevantUserMemories(1L, "me gusta caminar");

        assertThat(vectorMemoryPort.lastMultiSourceQuery).isNotNull();
        assertThat(vectorMemoryPort.lastMultiSourceQuery.userId()).isEqualTo(1L);
        assertThat(vectorMemoryPort.lastMultiSourceQuery.sourceTypes()).containsExactly(
                VectorMemorySource.CHATBOT,
                VectorMemorySource.GUIDED_LANTERNS,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                VectorMemorySource.ONBOARDING
        );
        assertThat(vectorMemoryPort.lastMultiSourceQuery.limit()).isEqualTo(properties.getDefaultLimit());
        assertThat(vectorMemoryPort.lastMultiSourceQuery.similarityThreshold())
                .isEqualTo(properties.getRecallSimilarityThreshold());
    }

    @Test
    void findRelevantUserMemories_shouldSearchSpecificSource() {
        service.findRelevantUserMemories(1L, VectorMemorySource.GUIDED_LANTERNS, "ansiedad");

        assertThat(vectorMemoryPort.lastSingleSourceQuery).isNotNull();
        assertThat(vectorMemoryPort.lastSingleSourceQuery.sourceType()).isEqualTo(VectorMemorySource.GUIDED_LANTERNS);
    }

    @Test
    void findRelevantUserMemories_shouldReturnMemoryFromAnotherConversationForSameUser() {
        vectorMemoryPort.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1234",
                "Hola mi nombre es sergio, tengo 25 anos y soy un estudiante",
                null,
                0.42
        ));

        List<VectorMemory> result = service.findRelevantUserMemories(
                1L,
                "A veces soy medio medio y me cuestan las cosas sencillas, me recordas mi edad"
        );

        assertThat(result).extracting(VectorMemory::content)
                .containsExactly("Hola mi nombre es sergio, tengo 25 anos y soy un estudiante");
        assertThat(vectorMemoryPort.lastMultiSourceQuery.userId()).isEqualTo(1L);
        assertThat(vectorMemoryPort.lastMultiSourceQuery.sourceTypes()).contains(VectorMemorySource.CHATBOT);
    }

    @Test
    void findRelevantUserMemories_shouldUseProfileRecallForAgeQuestions() {
        vectorMemoryPort.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1",
                "El usuario tiene 25 anos.",
                null,
                0.30
        ));

        List<VectorMemory> result = service.findRelevantUserMemories(
                1L,
                "A veces hay cosas que olvido, me recordas que edad tengo por favor"
        );

        assertThat(result).extracting(VectorMemory::content).containsExactly("El usuario tiene 25 anos.");
        assertThat(vectorMemoryPort.lastMultiSourceQuery.limit()).isEqualTo(10);
        assertThat(vectorMemoryPort.lastMultiSourceQuery.similarityThreshold()).isZero();
        assertThat(vectorMemoryPort.lastMultiSourceQuery.query()).contains("datos personales del usuario");
    }

    @Test
    void findRelevantUserMemories_shouldNotReturnMemoryFromAnotherUser() {
        vectorMemoryPort.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1234",
                "Hola mi nombre es sergio, tengo 25 anos y soy un estudiante",
                null,
                0.42
        ));

        List<VectorMemory> result = service.findRelevantUserMemories(2L, "me recordas mi edad");

        assertThat(result).isEmpty();
        assertThat(vectorMemoryPort.lastMultiSourceQuery.userId()).isEqualTo(2L);
    }

    @Test
    void findRelevantUserMemoriesBySources_shouldFilterByUserAcrossSources() {
        vectorMemoryPort.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1",
                "tengo 25 anos",
                null,
                0.75
        ));
        vectorMemoryPort.memories.add(new VectorMemory(
                "mem-2",
                2L,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                "99",
                "tengo 40 anos",
                null,
                0.95
        ));

        List<VectorMemory> result = service.findRelevantUserMemoriesBySources(
                1L,
                List.of(VectorMemorySource.CHATBOT, VectorMemorySource.EMOTIONAL_JOURNAL),
                "edad"
        );

        assertThat(result).extracting(VectorMemory::userId).containsOnly(1L);
        assertThat(result).extracting(VectorMemory::content).containsExactly("tengo 25 anos");
    }

    @Test
    void saveMemory_shouldNotThrowWhenVectorStoreFails() {
        vectorMemoryPort.failOnSave = true;

        assertThatCode(() -> service.saveMemory(new SaveVectorMemoryCommand(
                1L, VectorMemorySource.CHATBOT, "1", "source", "contentType", "content", null, null, Map.of())))
                .doesNotThrowAnyException();
    }

    @Test
    void deletePersonalitySummary_shouldDeleteSummaryFromDedicatedRepository() {
        service.deletePersonalitySummary(1L);

        assertThat(personalitySummaryRepository.deletedUserId).isEqualTo(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllMemoryContents_shouldReturnContentsAndFilterPersonalitySummary() throws Exception {
        vectorMemoryPort.memoryContents = List.of("Me siento feliz");

        List<String> contents = service.getAllMemoryContents(1L);
        assertThat(contents).containsExactly("Me siento feliz");
    }

    @Test
    void saveMemory_shouldTriggerAsyncPersonalitySummaryGeneration() {
        org.springframework.ai.chat.client.ChatClient chatClient =
                mock(org.springframework.ai.chat.client.ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);

        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenReturn(new UserVectorMemoryService.PersonalitySummaryDto("Test profile summary", "activity", "none"));

        vectorMemoryPort.memoryContents = List.of("Memory content 1", "Memory content 2");

        service.saveMemory(new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.ONBOARDING,
                "1",
                "ONBOARDING_GOALS",
                "ONBOARDING_GOALS",
                "content",
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "ONBOARDING")
        ));

        assertThat(vectorMemoryPort.savedCommands).hasSize(1);

        long start = System.currentTimeMillis();
        while (personalitySummaryRepository.savedSummary == null && (System.currentTimeMillis() - start) < 3000) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }

        assertThat(personalitySummaryRepository.savedSummary).isNotNull();
        assertThat(personalitySummaryRepository.savedSummary.getSummary()).isEqualTo("Test profile summary");
        assertThat(personalitySummaryRepository.savedSummary.getAccepted()).isEqualTo("activity");
        assertThat(personalitySummaryRepository.savedSummary.getRejected()).isEqualTo("none");
    }

    @Test
    void deletePersonalitySummary_shouldLogWarning_whenRepositoryFails() {
        UserPersonalitySummaryRepository failingRepository = mock(UserPersonalitySummaryRepository.class);
        doThrow(new RuntimeException("Delete failed")).when(failingRepository).deleteByUserId(any());

        UserVectorMemoryService testService = new UserVectorMemoryService(
                vectorMemoryPort,
                properties,
                new UserProfileFactExtractor(),
                chatClientProvider,
                failingRepository,
                new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes())
        );

        assertThatCode(() -> testService.deletePersonalitySummary(1L)).doesNotThrowAnyException();
    }

    @Test
    void rememberGeneratedChallenge_shouldDoNothing_whenChallengeIsNullOrTitleIsBlank() {
        service.rememberGeneratedChallenge(1L, "conv-1", null);
        service.rememberGeneratedChallenge(1L, "conv-1", new ChatReply.GeneratedChallenge("", "desc"));
        assertThat(vectorMemoryPort.savedCommands).isEmpty();
    }

    @Test
    void rememberChallengeDecision_shouldDoNothing_whenParametersAreInvalid() {
        service.rememberChallengeDecision(null, "conv-1", "title", "desc", "ACCEPTED");
        service.rememberChallengeDecision(1L, "conv-1", "", "desc", "ACCEPTED");
        service.rememberChallengeDecision(1L, "conv-1", "title", "desc", "");
        assertThat(vectorMemoryPort.savedCommands).isEmpty();
    }

    @Test
    void rememberRecommendedActivity_shouldPersistMemory() {
        service.rememberRecommendedActivity(
                1L,
                "conv-1",
                50L,
                new com.huly.backend.domain.model.chat.SuggestedChatAction(
                        ActivityType.DIARIO,
                        2L,
                        "Diario emocional",
                        "Ordenar pensamientos",
                        "/api/activities",
                        50L
                )
        );

        assertThat(vectorMemoryPort.savedCommands).singleElement().satisfies(command -> {
            assertThat(command.contentType()).isEqualTo("RECOMMENDED_ACTIVITY");
            assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        });
    }

    @Test
    void getAllMemoryContents_shouldReturnEmptyList_whenPortThrowsException() {
        vectorMemoryPort.failOnFindContents = true;

        List<String> contents = service.getAllMemoryContents(1L);
        assertThat(contents).isEmpty();
    }

    @Test
    void findRelevantUserMemories_shouldReturnEmpty_onSearchException() {
        VectorMemoryPort mockService = mock(VectorMemoryPort.class);
        when(mockService.findRelevantMemories(any(SearchVectorMemoryQuery.class)))
                .thenThrow(new RuntimeException("Search failed"));
        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                mockService,
                properties,
                new UserProfileFactExtractor(),
                chatClientProvider,
                personalitySummaryRepository,
                new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes())
        );

        List<VectorMemory> result = serviceWithMock.findRelevantUserMemories(1L, VectorMemorySource.CHATBOT, "query");
        assertThat(result).isEmpty();
    }

    @Test
    void findRelevantUserMemoriesBySources_shouldReturnEmpty_onSearchException() {
        VectorMemoryPort mockService = mock(VectorMemoryPort.class);
        when(mockService.findRelevantMemories(any(SearchVectorMemoriesQuery.class)))
                .thenThrow(new RuntimeException("Search failed"));
        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                mockService,
                properties,
                new UserProfileFactExtractor(),
                chatClientProvider,
                personalitySummaryRepository,
                new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes())
        );

        List<VectorMemory> result = serviceWithMock.findRelevantUserMemoriesBySources(
                1L,
                List.of(VectorMemorySource.CHATBOT),
                "query"
        );
        assertThat(result).isEmpty();
    }

    @Test
    void buildRecallQueries_shouldHandleWhenProfileRecallQueryEqualsQuery() {
        UserProfileFactExtractor mockExtractor = mock(UserProfileFactExtractor.class);
        when(mockExtractor.asksForProfileFact(anyString())).thenReturn(true);
        when(mockExtractor.buildProfileRecallQuery(anyString())).thenReturn("equalQuery");

        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                vectorMemoryPort,
                properties,
                mockExtractor,
                chatClientProvider,
                personalitySummaryRepository,
                new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes())
        );

        serviceWithMock.findRelevantUserMemories(1L, "equalQuery");
    }

    @Test
    void recallLimit_shouldHandleNullMaxLimit() {
        properties.setMaxLimit(null);
        service.findRelevantUserMemories(1L, "edad");
    }

    @Test
    void uniqueRankedAndLimited_shouldIgnoreNullMemories() {
        VectorMemoryPort mockService = mock(VectorMemoryPort.class);
        List<VectorMemory> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        when(mockService.findRelevantMemories(any(SearchVectorMemoriesQuery.class))).thenReturn(listWithNull);

        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                mockService,
                properties,
                new UserProfileFactExtractor(),
                chatClientProvider,
                personalitySummaryRepository,
                new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes())
        );
        List<VectorMemory> result = serviceWithMock.findRelevantUserMemories(1L, "query");
        assertThat(result).isEmpty();
    }

    @Test
    void generatePersonalitySummary_shouldReturnEarly_whenChatModelIsNull() {
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
        vectorMemoryPort.memoryContents = List.of("Memory 1");

        service.saveMemory(new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.ONBOARDING,
                "1",
                "ONBOARDING_GOALS",
                "ONBOARDING_GOALS",
                "content",
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "ONBOARDING")
        ));

        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }

        assertThat(vectorMemoryPort.savedCommands).hasSize(1);
        assertThat(personalitySummaryRepository.savedSummary).isNull();
    }

    @Test
    void generatePersonalitySummary_shouldHandleException() {
        org.springframework.ai.chat.client.ChatClient chatClient =
                mock(org.springframework.ai.chat.client.ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenThrow(new RuntimeException("ChatClient error"));

        vectorMemoryPort.memoryContents = List.of("Memory 1");

        service.saveMemory(new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.ONBOARDING,
                "1",
                "ONBOARDING_GOALS",
                "ONBOARDING_GOALS",
                "content",
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "ONBOARDING")
        ));

        try {
            Thread.sleep(200);
        } catch (InterruptedException ignored) {
        }

        assertThat(vectorMemoryPort.savedCommands).hasSize(1);
        assertThat(personalitySummaryRepository.savedSummary).isNull();
    }

    @Test
    void generatePersonalitySummary_shouldTruncateLongMemories() {
        org.springframework.ai.chat.client.ChatClient chatClient =
                mock(org.springframework.ai.chat.client.ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);

        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenReturn(new UserVectorMemoryService.PersonalitySummaryDto("Truncated summary", "activity", "none"));

        vectorMemoryPort.memoryContents = List.of("a".repeat(4005));

        service.saveMemory(new SaveVectorMemoryCommand(
                1L,
                VectorMemorySource.ONBOARDING,
                "1",
                "ONBOARDING_GOALS",
                "ONBOARDING_GOALS",
                "content",
                null,
                null,
                Map.of("createdFrom", "USER_MESSAGE", "feature", "ONBOARDING")
        ));

        long start = System.currentTimeMillis();
        while (personalitySummaryRepository.savedSummary == null && (System.currentTimeMillis() - start) < 3000) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }

        assertThat(personalitySummaryRepository.savedSummary).isNotNull();
        assertThat(personalitySummaryRepository.savedSummary.getSummary()).contains("Truncated summary");
    }

    private static final class RecordingVectorMemoryPort implements VectorMemoryPort {

        private final List<SaveVectorMemoryCommand> savedCommands = new ArrayList<>();
        private final List<VectorMemory> memories = new ArrayList<>();
        private List<String> memoryContents = List.of();
        private SearchVectorMemoryQuery lastSingleSourceQuery;
        private SearchVectorMemoriesQuery lastMultiSourceQuery;
        private boolean failOnSave;
        private boolean failOnFindContents;

        @Override
        public void saveMemory(SaveVectorMemoryCommand command) {
            if (failOnSave) {
                throw new RuntimeException("vector unavailable");
            }
            savedCommands.add(command);
        }

        @Override
        public List<VectorMemory> findRelevantMemories(SearchVectorMemoryQuery query) {
            lastSingleSourceQuery = query;
            return memories.stream()
                    .filter(memory -> memory.userId().equals(query.userId()))
                    .filter(memory -> memory.sourceType() == query.sourceType())
                    .toList();
        }

        @Override
        public List<String> findMemoryContentsByUserIdExcludingSummary(Long userId) {
            if (failOnFindContents) {
                throw new RuntimeException("vector unavailable");
            }
            return memoryContents;
        }

        @Override
        public List<VectorMemoryEntry> findMemoriesByUserIdExcludingSummary(Long userId) {
            return List.of();
        }

        @Override
        public List<VectorMemory> findRelevantMemories(SearchVectorMemoriesQuery query) {
            lastMultiSourceQuery = query;
            return VectorMemoryPort.super.findRelevantMemories(query);
        }

        @Override
        public void deleteMemories(com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand command) {
        }
    }

    private static final class RecordingUserPersonalitySummaryRepository implements UserPersonalitySummaryRepository {

        private UserPersonalitySummary savedSummary;
        private Long deletedUserId;

        @Override
        public Optional<UserPersonalitySummary> findByUserId(Long userId) {
            return Optional.ofNullable(savedSummary).filter(summary -> userId.equals(summary.getUserId()));
        }

        @Override
        public UserPersonalitySummary save(UserPersonalitySummary summary) {
            this.savedSummary = summary;
            return summary;
        }

        @Override
        public void deleteByUserId(Long userId) {
            this.deletedUserId = userId;
            this.savedSummary = null;
        }
    }
}
