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

class UserVectorMemoryServiceTest {

    private VectorMemoryProperties properties;
    private RecordingVectorMemoryService vectorMemoryService;
    private UserVectorMemoryService service;

    @BeforeEach
    void setUp() {
        properties = new VectorMemoryProperties();
        vectorMemoryService = new RecordingVectorMemoryService();
        service = new UserVectorMemoryService(vectorMemoryService, properties, new UserProfileFactExtractor());
    }

    @Test
    void findRelevantUserMemories_shouldSearchAcrossAllUserMemorySources() {
        service.findRelevantUserMemories(1L, "me gusta caminar");

        assertThat(vectorMemoryService.lastMultiSourceQuery).isNotNull();
        assertThat(vectorMemoryService.lastMultiSourceQuery.userId()).isEqualTo(1L);
        assertThat(vectorMemoryService.lastMultiSourceQuery.sourceTypes()).containsExactly(
                VectorMemorySource.CHATBOT,
                VectorMemorySource.GUIDED_LANTERNS,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                VectorMemorySource.ONBOARDING
        );
        assertThat(vectorMemoryService.lastMultiSourceQuery.limit()).isEqualTo(properties.getDefaultLimit());
        assertThat(vectorMemoryService.lastMultiSourceQuery.similarityThreshold())
                .isEqualTo(properties.getRecallSimilarityThreshold());
    }

    @Test
    void findRelevantUserMemories_shouldSearchSpecificSource() {
        service.findRelevantUserMemories(1L, VectorMemorySource.GUIDED_LANTERNS, "ansiedad");

        assertThat(vectorMemoryService.lastSingleSourceQuery).isNotNull();
        assertThat(vectorMemoryService.lastSingleSourceQuery.sourceType()).isEqualTo(VectorMemorySource.GUIDED_LANTERNS);
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
    void rememberGuidedLanternInput_shouldBuildGuidedLanternMemoryCommand() {
        service.rememberGuidedLanternInput(7L, "lantern-1", "me cuesta soltar lo que paso");

        SaveVectorMemoryCommand command = vectorMemoryService.savedCommands.get(0);
        assertThat(command.sourceType()).isEqualTo(VectorMemorySource.GUIDED_LANTERNS);
        assertThat(command.source()).isEqualTo("GUIDED_LANTERN_INPUT");
        assertThat(command.contentType()).isEqualTo("GUIDED_LANTERN_INPUT");
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

    private static final class RecordingVectorMemoryService implements VectorMemoryService {

        private final List<SaveVectorMemoryCommand> savedCommands = new ArrayList<>();
        private final List<VectorMemory> memories = new ArrayList<>();
        private SearchVectorMemoryQuery lastSingleSourceQuery;
        private SearchVectorMemoriesQuery lastMultiSourceQuery;
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
            // No se usa en este test.
        }
    }
}
