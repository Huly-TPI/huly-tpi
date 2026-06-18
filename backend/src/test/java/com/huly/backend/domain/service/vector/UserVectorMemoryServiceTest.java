package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.provider.VectorMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class UserVectorMemoryServiceTest {

    private VectorMemoryProperties properties;
    private RecordingVectorMemoryService vectorMemoryService;
    private UserVectorMemoryService service;
    private org.springframework.beans.factory.ObjectProvider<org.springframework.ai.chat.client.ChatClient> chatClientProvider;
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new VectorMemoryProperties();
        vectorMemoryService = new RecordingVectorMemoryService();
        chatClientProvider = org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class);
        jdbcTemplate = org.mockito.Mockito.mock(org.springframework.jdbc.core.JdbcTemplate.class);
        service = new UserVectorMemoryService(vectorMemoryService, properties, new UserProfileFactExtractor(), chatClientProvider, jdbcTemplate, new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()));
    }

    @Test
    void findRelevantUserMemories_shouldSearchAcrossAllUserMemorySources() {
        service.findRelevantUserMemories(1L, "me gusta caminar");

        assertThat(vectorMemoryService.lastMultiSourceQuery).isNotNull();
        assertThat(vectorMemoryService.lastMultiSourceQuery.userId()).isEqualTo(1L);
        assertThat(vectorMemoryService.lastMultiSourceQuery.sourceTypes()).containsExactly(
                VectorMemorySource.CHATBOT,
                VectorMemorySource.GUIDED_CLOUDS,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                VectorMemorySource.ONBOARDING
        );
        assertThat(vectorMemoryService.lastMultiSourceQuery.limit()).isEqualTo(properties.getDefaultLimit());
        assertThat(vectorMemoryService.lastMultiSourceQuery.similarityThreshold())
                .isEqualTo(properties.getRecallSimilarityThreshold());
    }

    @Test
    void findRelevantUserMemories_shouldSearchSpecificSource() {
        service.findRelevantUserMemories(1L, VectorMemorySource.GUIDED_CLOUDS, "ansiedad");

        assertThat(vectorMemoryService.lastSingleSourceQuery).isNotNull();
        assertThat(vectorMemoryService.lastSingleSourceQuery.sourceType()).isEqualTo(VectorMemorySource.GUIDED_CLOUDS);
    }

    @Test
    void findRelevantUserMemories_shouldReturnMemoryFromAnotherConversationForSameUser() {
        vectorMemoryService.memories.add(new VectorMemory(
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
        assertThat(vectorMemoryService.lastMultiSourceQuery.userId()).isEqualTo(1L);
        assertThat(vectorMemoryService.lastMultiSourceQuery.sourceTypes()).contains(VectorMemorySource.CHATBOT);
    }

    @Test
    void findRelevantUserMemories_shouldUseProfileRecallForAgeQuestions() {
        vectorMemoryService.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1",
                "El usuario tiene 25 años.",
                null,
                0.30
        ));

        List<VectorMemory> result = service.findRelevantUserMemories(
                1L,
                "A veces hay cosas que olvido, me recordas que edad tengo por favor"
        );

        assertThat(result).extracting(VectorMemory::content).containsExactly("El usuario tiene 25 años.");
        assertThat(vectorMemoryService.lastMultiSourceQuery.limit()).isEqualTo(10);
        assertThat(vectorMemoryService.lastMultiSourceQuery.similarityThreshold()).isZero();
        assertThat(vectorMemoryService.lastMultiSourceQuery.query()).contains("datos personales del usuario");
    }

    @Test
    void findRelevantUserMemories_shouldNotReturnMemoryFromAnotherUser() {
        vectorMemoryService.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1234",
                "Hola mi nombre es sergio, tengo 25 anos y soy un estudiante",
                null,
                0.42
        ));

        List<VectorMemory> result = service.findRelevantUserMemories(
                2L,
                "me recordas mi edad"
        );

        assertThat(result).isEmpty();
        assertThat(vectorMemoryService.lastMultiSourceQuery.userId()).isEqualTo(2L);
    }

    @Test
    void findRelevantUserMemoriesBySources_shouldFilterByUserAcrossSources() {
        vectorMemoryService.memories.add(new VectorMemory(
                "mem-1",
                1L,
                VectorMemorySource.CHATBOT,
                "1",
                "tengo 25 anos",
                null,
                0.75
        ));
        vectorMemoryService.memories.add(new VectorMemory(
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
    void rememberChatMessage_shouldBuildChatbotMemoryCommand() {
        service.rememberChatMessage(7L, "conv-1", "me gusta jugar a la play");

        SaveVectorMemoryCommand command = vectorMemoryService.savedCommands.get(0);
        assertThat(command.userId()).isEqualTo(7L);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(command.sourceId()).isEqualTo("7");
        assertThat(command.conversationId()).isEqualTo("conv-1");
        assertThat(command.source()).isEqualTo("USER_CHAT_MESSAGE");
        assertThat(command.contentType()).isEqualTo("CHAT_MESSAGE");
        assertThat(command.content()).isEqualTo("me gusta jugar a la play");
        assertThat(command.metadata()).containsEntry("feature", "CHATBOT");
    }

    @Test
    void rememberChatMessage_shouldSaveProfileFactsWhenMessageContainsNameAgeAndStudent() {
        service.rememberChatMessage(7L, "4567", "Buenas, mi nombre es Sergio tengo 25 anos y soy estudiante");

        assertThat(vectorMemoryService.savedCommands).hasSize(2);
        SaveVectorMemoryCommand profileFacts = vectorMemoryService.savedCommands.get(1);
        assertThat(profileFacts.userId()).isEqualTo(7L);
        assertThat(profileFacts.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(profileFacts.sourceId()).isEqualTo("7");
        assertThat(profileFacts.conversationId()).isEqualTo("4567");
        assertThat(profileFacts.source()).isEqualTo("USER_PROFILE_FACTS");
        assertThat(profileFacts.contentType()).isEqualTo("PROFILE_FACTS");
        assertThat(profileFacts.content())
                .contains("El usuario se llama Sergio.")
                .contains("El usuario tiene 25 años.")
                .contains("El usuario es estudiante.");
        assertThat(profileFacts.metadata()).containsEntry("feature", "CHATBOT_PROFILE");
    }

    @Test
    void rememberGuidedCloudInput_shouldBuildGuidedCloudMemoryCommand() {
        service.rememberGuidedCloudInput(7L, "cloud-1", "me cuesta soltar lo que paso");

        SaveVectorMemoryCommand command = vectorMemoryService.savedCommands.get(0);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.GUIDED_CLOUDS);
        assertThat(command.source()).isEqualTo("GUIDED_CLOUD_INPUT");
        assertThat(command.contentType()).isEqualTo("GUIDED_CLOUD_INPUT");
    }

    @Test
    void rememberJournalEntry_shouldBuildEmotionalJournalMemoryCommand() {
        service.rememberJournalEntry(7L, 99L, "hoy escribi algo personal");

        SaveVectorMemoryCommand command = vectorMemoryService.savedCommands.get(0);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.EMOTIONAL_JOURNAL);
        assertThat(command.sourceId()).isEqualTo("99");
        assertThat(command.source()).isEqualTo("EMOTIONAL_JOURNAL_ENTRY");
        assertThat(command.contentType()).isEqualTo("JOURNAL_ENTRY");
    }

    @Test
    void rememberChatMessage_shouldNotThrowWhenVectorStoreFails() {
        vectorMemoryService.failOnSave = true;

        assertThatCode(() -> service.rememberChatMessage(1L, "conv-1", "me gusta caminar"))
                .doesNotThrowAnyException();
    }

    @Test
    void rememberRecommendedActivity_shouldSaveActivityMemory() {
        com.huly.backend.domain.model.chat.SuggestedChatAction action = new com.huly.backend.domain.model.chat.SuggestedChatAction(
                com.huly.backend.domain.model.enums.ActivityType.RESPIRACION,
                100L,
                "Respira hondo",
                "Inhala y exhala",
                null,
                null
        );

        service.rememberRecommendedActivity(1L, "conv-123", 200L, action);

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand cmd = vectorMemoryService.savedCommands.get(0);
        assertThat(cmd.userId()).isEqualTo(1L);
        assertThat(cmd.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(cmd.sourceId()).isEqualTo("200");
        assertThat(cmd.conversationId()).isEqualTo("conv-123");
        assertThat(cmd.source()).isEqualTo("RECOMMENDED_ACTIVITY");
        assertThat(cmd.contentType()).isEqualTo("RECOMMENDED_ACTIVITY");
        assertThat(cmd.content()).contains("Huly recomendo la actividad: Respira hondo");
        assertThat(cmd.metadata()).containsEntry("feature", "CHATBOT_ACTIVITY_RECOMMENDATION");
    }

    @Test
    void rememberActivityRecommendationDecision_shouldSaveDecisionMemory() {
        com.huly.backend.domain.model.EmotionalEvent event = com.huly.backend.domain.model.EmotionalEvent.builder()
                .id(300L)
                .userId(1L)
                .recommendationDecision(com.huly.backend.domain.model.enums.RecommendationDecision.ACCEPTED)
                .recommendedActivityId(100L)
                .chosenActivityId(100L)
                .generatedRecommendation("Respira hondo")
                .build();

        service.rememberActivityRecommendationDecision(event);

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand cmd = vectorMemoryService.savedCommands.get(0);
        assertThat(cmd.userId()).isEqualTo(1L);
        assertThat(cmd.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(cmd.sourceId()).isEqualTo("300");
        assertThat(cmd.source()).isEqualTo("ACTIVITY_RECOMMENDATION_DECISION");
        assertThat(cmd.content()).contains("El usuario acepto la recomendacion");
        assertThat(cmd.metadata()).containsEntry("feature", "CHATBOT_ACTIVITY_DECISION");
    }

    @Test
    void rememberGeneratedChallenge_shouldSaveChallengeMemory() {
        com.huly.backend.domain.model.chat.ChatReply.GeneratedChallenge challenge = new com.huly.backend.domain.model.chat.ChatReply.GeneratedChallenge(
                "Estiramiento",
                "Estira tus musculos por 5 minutos"
        );

        service.rememberGeneratedChallenge(1L, "conv-1", challenge);

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand cmd = vectorMemoryService.savedCommands.get(0);
        assertThat(cmd.userId()).isEqualTo(1L);
        assertThat(cmd.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(cmd.content()).contains("Huly sugirio el reto: Estiramiento");
        assertThat(cmd.metadata()).containsEntry("feature", "CHATBOT_CHALLENGE");
    }

    @Test
    void rememberChallengeDecision_shouldSaveChallengeDecisionMemory() {
        service.rememberChallengeDecision(1L, "conv-1", "Estiramiento", "Estira tus musculos", "ACCEPTED");

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand cmd = vectorMemoryService.savedCommands.get(0);
        assertThat(cmd.userId()).isEqualTo(1L);
        assertThat(cmd.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(cmd.content()).contains("El usuario acepto el reto: Estiramiento");
        assertThat(cmd.metadata()).containsEntry("feature", "CHATBOT_CHALLENGE_DECISION");
    }

    @Test
    void deletePersonalitySummary_shouldDeleteMemory() {
        service.deletePersonalitySummary(1L);
        assertThat(vectorMemoryService.deletedCommand).isNotNull();
        assertThat(vectorMemoryService.deletedCommand.userId()).isEqualTo(1L);
        assertThat(vectorMemoryService.deletedCommand.sourceType()).isEqualTo(VectorMemorySource.CHATBOT);
        assertThat(vectorMemoryService.deletedCommand.sourceId()).isEqualTo("personality-summary");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getAllMemoryContents_shouldReturnContentsAndFilterPersonalitySummary() throws Exception {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenAnswer(invocation -> {
                    org.springframework.jdbc.core.RowMapper<String> mapper = invocation.getArgument(1);
                    java.sql.ResultSet rs1 = org.mockito.Mockito.mock(java.sql.ResultSet.class);
                    when(rs1.getString("content")).thenReturn("Me siento feliz");
                    when(rs1.getString("metadata")).thenReturn("{\"userId\": 1, \"contentType\": \"TEXT_MEMORY\"}");
                    String res1 = mapper.mapRow(rs1, 0);

                    List<String> list = new ArrayList<>();
                    if (res1 != null) list.add(res1);
                    return list;
                });

        List<String> contents = service.getAllMemoryContents(1L);
        assertThat(contents).containsExactly("Me siento feliz");
    }

    @Test
    void rememberOnboardingGoals_shouldTriggerAsyncPersonalitySummaryGeneration() {
        org.springframework.ai.chat.client.ChatClient chatClient = mock(org.springframework.ai.chat.client.ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        
        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenReturn(new UserVectorMemoryService.PersonalitySummaryDto("Test profile summary", "activity", "none"));

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenReturn(List.of("Memory content 1", "Memory content 2"));

        service.rememberOnboardingGoals(1L, "Goal1", "Goal2", "Goal3");

        assertThat(vectorMemoryService.savedCommands).isNotEmpty();
        
        long start = System.currentTimeMillis();
        while (vectorMemoryService.savedCommands.size() < 2 && (System.currentTimeMillis() - start) < 3000) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        
        assertThat(vectorMemoryService.savedCommands).hasSize(2);
        SaveVectorMemoryCommand personalityCommand = vectorMemoryService.savedCommands.get(1);
        assertThat(personalityCommand.contentType()).isEqualTo("PERSONALITY_SUMMARY");
        assertThat(personalityCommand.content()).contains("Test profile summary");
    }

    @Test
    void generatePersonalitySummary_shouldHandleNullChatModelAndEmptyMemories() {
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenReturn(List.of());

        service.rememberOnboardingGoals(1L, "Goal1", "Goal2", "Goal3");

        try { Thread.sleep(200); } catch (InterruptedException e) {}

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
    }

    @Test
    void rememberRecommendedActivity_shouldDoNothing_whenActionIsNull() {
        service.rememberRecommendedActivity(1L, "conv-1", 100L, null);
        assertThat(vectorMemoryService.savedCommands).isEmpty();
    }

    @Test
    void rememberActivityRecommendationDecision_shouldDoNothing_whenParametersAreInvalid() {
        service.rememberActivityRecommendationDecision(null);
        service.rememberActivityRecommendationDecision(com.huly.backend.domain.model.EmotionalEvent.builder().build());
        assertThat(vectorMemoryService.savedCommands).isEmpty();
    }

    @Test
    void rememberGeneratedChallenge_shouldDoNothing_whenChallengeIsNullOrTitleIsBlank() {
        service.rememberGeneratedChallenge(1L, "conv-1", null);
        service.rememberGeneratedChallenge(1L, "conv-1", new com.huly.backend.domain.model.chat.ChatReply.GeneratedChallenge("", "desc"));
        assertThat(vectorMemoryService.savedCommands).isEmpty();
    }

    @Test
    void rememberChallengeDecision_shouldDoNothing_whenParametersAreInvalid() {
        service.rememberChallengeDecision(null, "conv-1", "title", "desc", "ACCEPTED");
        service.rememberChallengeDecision(1L, "conv-1", "", "desc", "ACCEPTED");
        service.rememberChallengeDecision(1L, "conv-1", "title", "desc", "");
        assertThat(vectorMemoryService.savedCommands).isEmpty();
    }

    @Test
    void deletePersonalitySummary_shouldLogWarning_whenVectorStoreFails() {
        VectorMemoryService throwingService = mock(VectorMemoryService.class);
        doThrow(new RuntimeException("Delete failed")).when(throwingService).deleteMemories(any());
        
        UserVectorMemoryService testService = new UserVectorMemoryService(throwingService, properties, new UserProfileFactExtractor(), chatClientProvider, jdbcTemplate, new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()));
        
        assertThatCode(() -> testService.deletePersonalitySummary(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void getAllMemoryContents_shouldReturnEmptyList_whenJdbcThrowsException() {
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenThrow(new RuntimeException("JDBC error"));

        List<String> contents = service.getAllMemoryContents(1L);
        assertThat(contents).isEmpty();
    }

    @Test
    void findRelevantUserMemories_shouldReturnEmpty_onSearchException() {
        VectorMemoryService mockService = mock(VectorMemoryService.class);
        when(mockService.findRelevantMemories(any(SearchVectorMemoryQuery.class)))
                .thenThrow(new RuntimeException("Search failed"));
        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                mockService, properties, new UserProfileFactExtractor(), chatClientProvider, jdbcTemplate, new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()));
        
        List<VectorMemory> result = serviceWithMock.findRelevantUserMemories(1L, VectorMemorySource.CHATBOT, "query");
        assertThat(result).isEmpty();
    }

    @Test
    void findRelevantUserMemoriesBySources_shouldReturnEmpty_onSearchException() {
        VectorMemoryService mockService = mock(VectorMemoryService.class);
        when(mockService.findRelevantMemories(any(SearchVectorMemoriesQuery.class)))
                .thenThrow(new RuntimeException("Search failed"));
        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                mockService, properties, new UserProfileFactExtractor(), chatClientProvider, jdbcTemplate, new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()));
        
        List<VectorMemory> result = serviceWithMock.findRelevantUserMemoriesBySources(1L, List.of(VectorMemorySource.CHATBOT), "query");
        assertThat(result).isEmpty();
    }

    @Test
    void buildRecallQueries_shouldHandleWhenProfileRecallQueryEqualsQuery() {
        UserProfileFactExtractor mockExtractor = mock(UserProfileFactExtractor.class);
        when(mockExtractor.asksForProfileFact(anyString())).thenReturn(true);
        when(mockExtractor.buildProfileRecallQuery(anyString())).thenReturn("equalQuery");
        
        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                vectorMemoryService, properties, mockExtractor, chatClientProvider, jdbcTemplate, new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()));
        
        serviceWithMock.findRelevantUserMemories(1L, "equalQuery");
    }

    @Test
    void recallLimit_shouldHandleNullMaxLimit() {
        properties.setMaxLimit(null);
        service.findRelevantUserMemories(1L, "edad");
    }

    @Test
    void uniqueRankedAndLimited_shouldIgnoreNullMemories() {
        VectorMemoryService mockService = mock(VectorMemoryService.class);
        List<VectorMemory> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        when(mockService.findRelevantMemories(any(SearchVectorMemoriesQuery.class))).thenReturn(listWithNull);
        
        UserVectorMemoryService serviceWithMock = new UserVectorMemoryService(
                mockService, properties, new UserProfileFactExtractor(), chatClientProvider, jdbcTemplate, new org.springframework.core.io.ByteArrayResource("mock prompt".getBytes()));
        List<VectorMemory> result = serviceWithMock.findRelevantUserMemories(1L, "query");
        assertThat(result).isEmpty();
    }

    @Test
    void generatePersonalitySummary_shouldReturnEarly_whenChatModelIsNull() {
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenReturn(List.of("Memory 1"));

        service.rememberOnboardingGoals(1L, "Goal1", "Goal2", "Goal3");

        try { Thread.sleep(200); } catch (InterruptedException e) {}

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
    }

    @Test
    void generatePersonalitySummary_shouldHandleException() {
        org.springframework.ai.chat.client.ChatClient chatClient = mock(org.springframework.ai.chat.client.ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenThrow(new RuntimeException("ChatClient error"));

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenReturn(List.of("Memory 1"));

        service.rememberOnboardingGoals(1L, "Goal1", "Goal2", "Goal3");

        try { Thread.sleep(200); } catch (InterruptedException e) {}

        assertThat(vectorMemoryService.savedCommands).hasSize(1);
    }

    @Test
    void saveMemory_shouldLogWarningWithNullUserId_whenSaveFails() {
        vectorMemoryService.failOnSave = true;
        assertThatCode(() -> service.rememberChatMessage(null, "conv-1", "msg"))
                .doesNotThrowAnyException();
    }

    @Test
    void rememberActivityRecommendationDecision_shouldHandleIgnoredAndChoseOther() {
        com.huly.backend.domain.model.EmotionalEvent eventIgnored = com.huly.backend.domain.model.EmotionalEvent.builder()
                .id(301L)
                .userId(1L)
                .recommendationDecision(com.huly.backend.domain.model.enums.RecommendationDecision.IGNORED)
                .build();
        service.rememberActivityRecommendationDecision(eventIgnored);
        assertThat(vectorMemoryService.savedCommands.get(0).content()).contains("El usuario rechazo la recomendacion");

        vectorMemoryService.savedCommands.clear();

        com.huly.backend.domain.model.EmotionalEvent eventChoseOther = com.huly.backend.domain.model.EmotionalEvent.builder()
                .id(302L)
                .userId(1L)
                .recommendationDecision(com.huly.backend.domain.model.enums.RecommendationDecision.CHOSE_OTHER)
                .build();
        service.rememberActivityRecommendationDecision(eventChoseOther);
        assertThat(vectorMemoryService.savedCommands.get(0).content()).contains("El usuario eligio otra actividad para la recomendacion");
    }

    @Test
    void rememberRecommendedActivity_shouldHandleNullActionTypeAndNullEmotionalEventId() {
        com.huly.backend.domain.model.chat.SuggestedChatAction actionNullType = new com.huly.backend.domain.model.chat.SuggestedChatAction(
                null,
                100L,
                "Title",
                "Description",
                null,
                null
        );
        service.rememberRecommendedActivity(1L, "conv-123", null, actionNullType);
        
        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand cmd = vectorMemoryService.savedCommands.get(0);
        assertThat(cmd.sourceId()).isEqualTo("1");
        assertThat(cmd.content()).contains("Tipo: UNKNOWN");
    }

    @Test
    void rememberJournalEntry_shouldHandleNullJournalEntryId() {
        service.rememberJournalEntry(1L, null, "content");
        assertThat(vectorMemoryService.savedCommands).hasSize(1);
        SaveVectorMemoryCommand cmd = vectorMemoryService.savedCommands.get(0);
        assertThat(cmd.sourceId()).isNull();
    }

    @Test
    void generatePersonalitySummary_shouldTruncateLongMemories() {
        org.springframework.ai.chat.client.ChatClient chatClient = mock(org.springframework.ai.chat.client.ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        
        when(chatClient.prompt().system(any(org.springframework.core.io.Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenReturn(new UserVectorMemoryService.PersonalitySummaryDto("Truncated summary", "activity", "none"));

        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), anyString()))
                .thenReturn(List.of("a".repeat(4005)));

        service.rememberOnboardingGoals(1L, "Goal1", "Goal2", "Goal3");

        long start = System.currentTimeMillis();
        while (vectorMemoryService.savedCommands.size() < 2 && (System.currentTimeMillis() - start) < 3000) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        
        assertThat(vectorMemoryService.savedCommands).hasSize(2);
        SaveVectorMemoryCommand personalityCommand = vectorMemoryService.savedCommands.get(1);
        assertThat(personalityCommand.content()).contains("Truncated summary");
    }

    private static final class RecordingVectorMemoryService implements VectorMemoryService {

        private final List<SaveVectorMemoryCommand> savedCommands = new ArrayList<>();
        private final List<VectorMemory> memories = new ArrayList<>();
        private SearchVectorMemoryQuery lastSingleSourceQuery;
        private SearchVectorMemoriesQuery lastMultiSourceQuery;
        private com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand deletedCommand;
        private boolean failOnSave;

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
        public List<VectorMemory> findRelevantMemories(SearchVectorMemoriesQuery query) {
            lastMultiSourceQuery = query;
            return VectorMemoryService.super.findRelevantMemories(query);
        }

        @Override
        public void deleteMemories(com.huly.backend.domain.model.vector.DeleteVectorMemoryCommand command) {
            this.deletedCommand = command;
        }
    }
}
