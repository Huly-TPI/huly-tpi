package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.user.UserPersonalitySummary;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoriesQuery;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemory;
import com.huly.backend.domain.model.vector.VectorMemoryEntry;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import com.huly.backend.domain.port.VectorMemoryPort;
import com.huly.backend.domain.repository.UserPersonalitySummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

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

    private static final Resource PROMPT = new ByteArrayResource("mock prompt".getBytes());

    private VectorMemoryProperties properties;
    private RecordingVectorMemoryPort vectorMemoryPort;
    private RecordingUserPersonalitySummaryRepository personalitySummaryRepository;
    private UserProfileFactExtractor userProfileFactExtractor;
    private ObjectProvider<ChatClient> chatClientProvider;
    private UserVectorMemoryService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new VectorMemoryProperties();
        vectorMemoryPort = new RecordingVectorMemoryPort();
        personalitySummaryRepository = new RecordingUserPersonalitySummaryRepository();
        userProfileFactExtractor = new UserProfileFactExtractor();
        chatClientProvider = mock(ObjectProvider.class);
        service = new UserVectorMemoryService(
                vectorMemoryPort,
                properties,
                userProfileFactExtractor,
                chatClientProvider,
                personalitySummaryRepository,
                PROMPT
        );
    }

    @Test
    @DisplayName("Busca en todas las fuentes de memoria del usuario")
    void findRelevantUserMemoriesShouldSearchAcrossAllUserMemorySources() {
        findAcrossAllSources(1L, "me gusta caminar");

        thenMultiSourceQueryTargetsAllSources();
        thenMultiSourceUserIdIs(1L);
        thenMultiSourceLimitIs(properties.getDefaultLimit());
        thenMultiSourceThresholdIs(properties.getRecallSimilarityThreshold());
    }

    @Test
    @DisplayName("Busca en una fuente específica de memoria")
    void findRelevantUserMemoriesShouldSearchSpecificSource() {
        findInSource(1L, VectorMemorySource.GUIDED_LANTERNS, "ansiedad");

        thenSingleSourceIs(VectorMemorySource.GUIDED_LANTERNS);
    }

    @Test
    @DisplayName("Recupera una memoria de otra conversación del mismo usuario")
    void findRelevantUserMemoriesShouldReturnMemoryFromAnotherConversationForSameUser() {
        givenStoredMemory(memory("mem-1", 1L, VectorMemorySource.CHATBOT, "1234",
                "Hola mi nombre es sergio, tengo 25 anos y soy un estudiante", 0.42));

        List<VectorMemory> result = findAcrossAllSources(
                1L, "A veces soy medio medio y me cuestan las cosas sencillas, me recordas mi edad");

        thenContentsAre(result, "Hola mi nombre es sergio, tengo 25 anos y soy un estudiante");
        thenMultiSourceUserIdIs(1L);
        thenMultiSourceIncludesChatbot();
    }

    @Test
    @DisplayName("Usa el recall de perfil para preguntas sobre la edad")
    void findRelevantUserMemoriesShouldUseProfileRecallForAgeQuestions() {
        givenStoredMemory(memory("mem-1", 1L, VectorMemorySource.CHATBOT, "1",
                "El usuario tiene 25 anos.", 0.30));

        List<VectorMemory> result = findAcrossAllSources(
                1L, "A veces hay cosas que olvido, me recordas que edad tengo por favor");

        thenContentsAre(result, "El usuario tiene 25 anos.");
        thenMultiSourceLimitIs(10);
        thenMultiSourceThresholdIsZero();
        thenMultiSourceQueryContains("datos personales del usuario");
    }

    @Test
    @DisplayName("No recupera memorias de otro usuario")
    void findRelevantUserMemoriesShouldNotReturnMemoryFromAnotherUser() {
        givenStoredMemory(memory("mem-1", 1L, VectorMemorySource.CHATBOT, "1234",
                "Hola mi nombre es sergio, tengo 25 anos y soy un estudiante", 0.42));

        List<VectorMemory> result = findAcrossAllSources(2L, "me recordas mi edad");

        thenEmptyMemories(result);
        thenMultiSourceUserIdIs(2L);
    }

    @Test
    @DisplayName("Filtra por usuario al buscar en varias fuentes")
    void findRelevantUserMemoriesBySourcesShouldFilterByUserAcrossSources() {
        givenStoredMemory(memory("mem-1", 1L, VectorMemorySource.CHATBOT, "1", "tengo 25 anos", 0.75));
        givenStoredMemory(memory("mem-2", 2L, VectorMemorySource.EMOTIONAL_JOURNAL, "99", "tengo 40 anos", 0.95));

        List<VectorMemory> result = findAcrossSources(
                1L, sources(VectorMemorySource.CHATBOT, VectorMemorySource.EMOTIONAL_JOURNAL), "edad");

        thenUserIdsAre(result, 1L);
        thenContentsAre(result, "tengo 25 anos");
    }

    @Test
    @DisplayName("No propaga la excepción cuando falla el almacén vectorial al guardar")
    void saveMemoryShouldNotThrowWhenVectorStoreFails() {
        givenVectorStoreFailsOnSave();

        thenSavingDoesNotThrow(command(1L, VectorMemorySource.CHATBOT, "contentType"));
    }

    @Test
    @DisplayName("Traga la excepción al guardar aunque el comando sea nulo")
    void saveMemoryShouldSwallowExceptionWhenCommandIsNull() {
        givenVectorStoreFailsOnSave();

        thenSavingDoesNotThrow(null);
    }

    @Test
    @DisplayName("No genera resumen asíncrono cuando el comando es nulo")
    void saveMemoryShouldSkipAsyncGenerationWhenCommandIsNull() {
        saveMemory(null);

        thenSavedCommandsHasSize(1);
        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("No genera resumen asíncrono cuando el userId es nulo")
    void saveMemoryShouldSkipAsyncGenerationWhenUserIdIsNull() {
        saveMemory(command(null, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedCommandsHasSize(1);
        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("No genera resumen asíncrono para contenido de tipo PERSONALITY_SUMMARY")
    void saveMemoryShouldSkipAsyncGenerationForPersonalitySummaryContentType() {
        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "PERSONALITY_SUMMARY"));

        thenSavedCommandsHasSize(1);
        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("Elimina el resumen de personalidad del repositorio dedicado")
    void deletePersonalitySummaryShouldDeleteSummaryFromDedicatedRepository() {
        deletePersonalitySummary(1L);

        thenDeletedUserIdIs(1L);
    }

    @Test
    @DisplayName("No propaga la excepción cuando el repositorio falla al eliminar el resumen")
    void deletePersonalitySummaryShouldLogWarningWhenRepositoryFails() {
        givenDeletingRepositoryFails();

        thenDeletingDoesNotThrow(1L);
    }

    @Test
    @DisplayName("Devuelve los contenidos de memoria filtrando el resumen de personalidad")
    void getAllMemoryContentsShouldReturnContentsAndFilterPersonalitySummary() {
        givenStoredMemoryContents("Me siento feliz");

        List<String> contents = getAllMemoryContents(1L);

        thenContentsExactly(contents, "Me siento feliz");
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el puerto lanza una excepción al leer contenidos")
    void getAllMemoryContentsShouldReturnEmptyListWhenPortThrowsException() {
        givenFindContentsFails();

        List<String> contents = getAllMemoryContents(1L);

        thenEmptyContents(contents);
    }

    @Test
    @DisplayName("Recuerda el mensaje de chat con sourceId basado en el userId")
    void rememberChatMessageShouldPersistMemoryWithUserSourceId() {
        rememberChatMessage(1L, "conv-1", "hoy trabaje mucho y estoy cansado");

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdIs("1");
        thenLastSavedContentTypeIs("CHAT_MESSAGE");
        thenLastSavedSourceTypeIs(VectorMemorySource.CHATBOT);
    }

    @Test
    @DisplayName("Recuerda el mensaje de chat con sourceId nulo cuando el userId es nulo")
    void rememberChatMessageShouldPersistMemoryWithNullSourceIdWhenUserIdIsNull() {
        rememberChatMessage(null, "conv-1", "hoy trabaje mucho y estoy cansado");

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdIsNull();
    }

    @Test
    @DisplayName("No recuerda el reto generado cuando el reto es nulo")
    void rememberGeneratedChallengeShouldDoNothingWhenChallengeIsNull() {
        rememberGeneratedChallenge(1L, "conv-1", null);

        thenSavedCommandsEmpty();
    }

    @Test
    @DisplayName("No recuerda el reto generado cuando el título es nulo")
    void rememberGeneratedChallengeShouldDoNothingWhenTitleIsNull() {
        rememberGeneratedChallenge(1L, "conv-1", challenge(null, "desc"));

        thenSavedCommandsEmpty();
    }

    @Test
    @DisplayName("No recuerda el reto generado cuando el título está en blanco")
    void rememberGeneratedChallengeShouldDoNothingWhenTitleIsBlank() {
        rememberGeneratedChallenge(1L, "conv-1", challenge("", "desc"));

        thenSavedCommandsEmpty();
    }

    @Test
    @DisplayName("Recuerda el reto generado con descripción y conversación válida")
    void rememberGeneratedChallengeShouldPersistMemoryWithDescriptionAndConversationId() {
        rememberGeneratedChallenge(1L, "conv-1", challenge("Reto matinal", "Descripcion del reto"));

        thenSavedCommandsHasSize(1);
        thenLastSavedContentTypeIs("GENERATED_CHALLENGE");
        thenLastSavedSourceIdContains("conv-1");
    }

    @Test
    @DisplayName("Recuerda el reto generado usando conversación desconocida cuando el id es nulo")
    void rememberGeneratedChallengeShouldUseUnknownConversationWhenConversationIdIsNull() {
        rememberGeneratedChallenge(1L, null, challenge("Reto nocturno", null));

        thenSavedCommandsHasSize(1);
        thenLastSavedContentTypeIs("GENERATED_CHALLENGE");
        thenLastSavedSourceIdContains("unknown");
    }

    @Test
    @DisplayName("Recuerda el reto generado usando conversación desconocida cuando el id está en blanco")
    void rememberGeneratedChallengeShouldUseUnknownConversationWhenConversationIdIsBlank() {
        rememberGeneratedChallenge(1L, "   ", challenge("Reto vespertino", "desc"));

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdContains("unknown");
    }

    @Test
    @DisplayName("Recuerda la actividad recomendada persistiendo la memoria")
    void rememberRecommendedActivityShouldPersistMemory() {
        rememberRecommendedActivity(1L, "conv-1", 50L, fullAction());

        thenSavedCommandsHasSize(1);
        thenLastSavedContentTypeIs("RECOMMENDED_ACTIVITY");
        thenLastSavedSourceTypeIs(VectorMemorySource.CHATBOT);
        thenLastSavedSourceIdIs("50");
    }

    @Test
    @DisplayName("No recuerda la actividad recomendada cuando la acción es nula")
    void rememberRecommendedActivityShouldDoNothingWhenActionIsNull() {
        rememberRecommendedActivity(1L, "conv-1", 50L, null);

        thenSavedCommandsEmpty();
    }

    @Test
    @DisplayName("Recuerda la actividad usando valores por defecto y el userId como sourceId")
    void rememberRecommendedActivityShouldUseDefaultsAndUserIdAsSourceId() {
        rememberRecommendedActivity(1L, "conv-1", null, emptyAction());

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdIs("1");
    }

    @Test
    @DisplayName("Recuerda la actividad con sourceId nulo cuando no hay evento ni usuario")
    void rememberRecommendedActivityShouldUseNullSourceIdWhenUserAndEventAreNull() {
        rememberRecommendedActivity(null, "conv-1", null, emptyAction());

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdIsNull();
    }

    @Test
    @DisplayName("Recuerda el rechazo de un reto persistiendo la memoria")
    void rememberChallengeDecisionShouldPersistMemory() {
        rememberChallengeDecision(1L, "conv-1", "Reto de respiración", "Respira hondo", "REJECTED");

        thenSavedCommandsHasSize(1);
        thenLastSavedContentTypeIs("CHALLENGE_DECISION");
    }

    @Test
    @DisplayName("Recuerda la aceptación de un reto con descripción nula y conversación desconocida")
    void rememberChallengeDecisionShouldPersistMemoryWithDefaults() {
        rememberChallengeDecision(1L, null, "Reto de respiración", null, "accepted");

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdContains("unknown");
    }

    @Test
    @DisplayName("Recuerda la decisión del reto usando conversación desconocida cuando el id está en blanco")
    void rememberChallengeDecisionShouldUseUnknownConversationWhenConversationIdIsBlank() {
        rememberChallengeDecision(1L, "   ", "Reto de gratitud", "Escribe 3 cosas", "ACCEPTED");

        thenSavedCommandsHasSize(1);
        thenLastSavedSourceIdContains("unknown");
    }

    @Test
    @DisplayName("No recuerda la decisión del reto cuando los parámetros son inválidos")
    void rememberChallengeDecisionShouldDoNothingWhenParametersAreInvalid() {
        rememberChallengeDecision(null, "conv-1", "title", "desc", "ACCEPTED");
        rememberChallengeDecision(1L, "conv-1", null, "desc", "ACCEPTED");
        rememberChallengeDecision(1L, "conv-1", "", "desc", "ACCEPTED");
        rememberChallengeDecision(1L, "conv-1", "title", "desc", null);
        rememberChallengeDecision(1L, "conv-1", "title", "desc", "");

        thenSavedCommandsEmpty();
    }

    @Test
    @DisplayName("Usa el límite por defecto cuando el máximo configurado es nulo")
    void recallLimitShouldHandleNullMaxLimit() {
        givenNullMaxLimit();

        findAcrossAllSources(1L, "edad");

        thenMultiSourceLimitIs(properties.getDefaultLimit());
    }

    @Test
    @DisplayName("Construye una sola consulta cuando el recall de perfil coincide con la consulta")
    void buildRecallQueriesShouldHandleWhenProfileRecallQueryEqualsQuery() {
        givenProfileRecallQueryEquals("equalQuery");

        findAcrossAllSources(1L, "equalQuery");

        thenMultiSourceQueryIs("equalQuery");
    }

    @Test
    @DisplayName("Devuelve vacío cuando la búsqueda multi-fuente lanza una excepción")
    void findRelevantUserMemoriesBySourcesShouldReturnEmptyOnSearchException() {
        givenSearchAcrossSourcesThrows();

        List<VectorMemory> result = findAcrossSources(1L, sources(VectorMemorySource.CHATBOT), "query");

        thenEmptyMemories(result);
    }

    @Test
    @DisplayName("Devuelve vacío cuando la búsqueda por fuente lanza una excepción")
    void findRelevantUserMemoriesShouldReturnEmptyOnSearchException() {
        givenSearchInSourceThrows();

        List<VectorMemory> result = findInSource(1L, VectorMemorySource.CHATBOT, "query");

        thenEmptyMemories(result);
    }

    @Test
    @DisplayName("Ignora las memorias nulas al deduplicar y ordenar")
    void uniqueRankedAndLimitedShouldIgnoreNullMemories() {
        givenSearchAcrossSourcesReturns(listWithNull());

        List<VectorMemory> result = findAcrossAllSources(1L, "query");

        thenEmptyMemories(result);
    }

    @Test
    @DisplayName("Deduplica por clave conservando la memoria con mayor puntaje")
    void uniqueRankedAndLimitedShouldDedupeByKeyKeepingHigherScore() {
        givenSearchAcrossSourcesReturns(dedupeMemories());

        List<VectorMemory> result = findAcrossAllSources(1L, "recuerdos varios");

        thenContentsAre(result, "A", "B", "C", "D");
        thenTopScoreIs(result, 0.9);
    }

    @Test
    @DisplayName("Genera y guarda el resumen de personalidad de forma asíncrona")
    void saveMemoryShouldTriggerAsyncPersonalitySummaryGeneration() {
        givenChatClientReturns(dto("Test profile summary", "activity", "none"));
        givenStoredMemoryContents("Memory content 1", "Memory content 2");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedCommandsHasSize(1);
        thenSavedSummaryEventuallyMatches("Test profile summary", "activity", "none");
    }

    @Test
    @DisplayName("Trunca las memorias largas antes de generar el resumen")
    void generatePersonalitySummaryShouldTruncateLongMemories() {
        givenChatClientReturns(dto("Truncated summary", "activity", "none"));
        givenStoredMemoryContents("a".repeat(4005));

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedSummaryEventuallyContains("Truncated summary");
    }

    @Test
    @DisplayName("No genera el resumen cuando no hay memorias suficientes")
    void generatePersonalitySummaryShouldReturnEarlyWhenNoMemories() {
        givenChatClientReturns(dto("summary", "a", "b"));
        givenStoredMemoryContents();

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("No genera el resumen cuando el modelo de chat no está disponible")
    void generatePersonalitySummaryShouldReturnEarlyWhenChatModelIsNull() {
        givenChatModelUnavailable();
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedCommandsHasSize(1);
        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("Maneja la excepción del modelo de chat al generar el resumen")
    void generatePersonalitySummaryShouldHandleException() {
        givenChatClientThrows();
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedCommandsHasSize(1);
        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("No guarda el resumen cuando el DTO devuelto es nulo")
    void generatePersonalitySummaryShouldNotSaveWhenDtoIsNull() {
        givenChatClientReturns(null);
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("No guarda el resumen cuando el resumen del DTO es nulo")
    void generatePersonalitySummaryShouldNotSaveWhenSummaryIsNull() {
        givenChatClientReturns(dto(null, "a", "b"));
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("No guarda el resumen cuando el resumen del DTO está en blanco")
    void generatePersonalitySummaryShouldNotSaveWhenSummaryIsBlank() {
        givenChatClientReturns(dto("   ", "a", "b"));
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSummaryNotSaved();
    }

    @Test
    @DisplayName("Anula los valores opcionales nulos o marcados como N/A del resumen")
    void generatePersonalitySummaryShouldNullifyNullAndNaOptionalValues() {
        givenChatClientReturns(dto("Profile", null, "N/A"));
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedSummaryEventuallyMatches("Profile", null, null);
    }

    @Test
    @DisplayName("Anula los valores opcionales en blanco y conserva los válidos del resumen")
    void generatePersonalitySummaryShouldNullifyBlankOptionalValuesAndKeepValid() {
        givenChatClientReturns(dto("Profile", "   ", "keep"));
        givenStoredMemoryContents("Memory 1");

        saveMemory(command(1L, VectorMemorySource.ONBOARDING, "ONBOARDING_GOALS"));

        thenSavedSummaryEventuallyMatches("Profile", null, "keep");
    }

    // --- arrange ---
    private void givenStoredMemory(VectorMemory memory) {
        vectorMemoryPort.memories.add(memory);
    }

    private void givenStoredMemoryContents(String... contents) {
        vectorMemoryPort.memoryContents = List.of(contents);
    }

    private void givenVectorStoreFailsOnSave() {
        vectorMemoryPort.failOnSave = true;
    }

    private void givenFindContentsFails() {
        vectorMemoryPort.failOnFindContents = true;
    }

    private void givenNullMaxLimit() {
        properties.setMaxLimit(null);
    }

    private void givenDeletingRepositoryFails() {
        UserPersonalitySummaryRepository failing = mock(UserPersonalitySummaryRepository.class);
        doThrow(new RuntimeException("Delete failed")).when(failing).deleteByUserId(any());
        service = new UserVectorMemoryService(
                vectorMemoryPort, properties, userProfileFactExtractor, chatClientProvider, failing, PROMPT);
    }

    private void givenProfileRecallQueryEquals(String value) {
        UserProfileFactExtractor mockExtractor = mock(UserProfileFactExtractor.class);
        when(mockExtractor.asksForProfileFact(anyString())).thenReturn(true);
        when(mockExtractor.buildProfileRecallQuery(anyString())).thenReturn(value);
        service = new UserVectorMemoryService(
                vectorMemoryPort, properties, mockExtractor, chatClientProvider, personalitySummaryRepository, PROMPT);
    }

    private void givenSearchAcrossSourcesThrows() {
        VectorMemoryPort mockPort = mock(VectorMemoryPort.class);
        when(mockPort.findRelevantMemories(any(SearchVectorMemoriesQuery.class)))
                .thenThrow(new RuntimeException("Search failed"));
        service = new UserVectorMemoryService(
                mockPort, properties, userProfileFactExtractor, chatClientProvider, personalitySummaryRepository, PROMPT);
    }

    private void givenSearchInSourceThrows() {
        VectorMemoryPort mockPort = mock(VectorMemoryPort.class);
        when(mockPort.findRelevantMemories(any(SearchVectorMemoryQuery.class)))
                .thenThrow(new RuntimeException("Search failed"));
        service = new UserVectorMemoryService(
                mockPort, properties, userProfileFactExtractor, chatClientProvider, personalitySummaryRepository, PROMPT);
    }

    private void givenSearchAcrossSourcesReturns(List<VectorMemory> memories) {
        VectorMemoryPort mockPort = mock(VectorMemoryPort.class);
        when(mockPort.findRelevantMemories(any(SearchVectorMemoriesQuery.class))).thenReturn(memories);
        service = new UserVectorMemoryService(
                mockPort, properties, userProfileFactExtractor, chatClientProvider, personalitySummaryRepository, PROMPT);
    }

    private void givenChatClientReturns(UserVectorMemoryService.PersonalitySummaryDto dto) {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt().system(any(Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenReturn(dto);
    }

    private void givenChatClientThrows() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt().system(any(Resource.class)).user(anyString()).call().entity(any(Class.class)))
                .thenThrow(new RuntimeException("ChatClient error"));
    }

    private void givenChatModelUnavailable() {
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
    }

    private VectorMemory memory(String id, Long userId, VectorMemorySource sourceType, String sourceId,
                                String content, Double score) {
        return new VectorMemory(id, userId, sourceType, sourceId, content, null, score);
    }

    private List<VectorMemory> listWithNull() {
        List<VectorMemory> memories = new ArrayList<>();
        memories.add(null);
        return memories;
    }

    private List<VectorMemory> dedupeMemories() {
        return List.of(
                memory("dup", 1L, VectorMemorySource.CHATBOT, "s", "A", 0.5),
                memory("dup", 1L, VectorMemorySource.CHATBOT, "s", "A", 0.9),
                memory("low", 1L, VectorMemorySource.CHATBOT, "s", "B", 0.8),
                memory("low", 1L, VectorMemorySource.CHATBOT, "s", "B", 0.2),
                memory(null, 1L, VectorMemorySource.CHATBOT, "s", "C", 0.3),
                memory("nul", 1L, VectorMemorySource.CHATBOT, "s", "D", null));
    }

    private List<VectorMemorySource> sources(VectorMemorySource... sourceTypes) {
        return List.of(sourceTypes);
    }

    private SaveVectorMemoryCommand command(Long userId, VectorMemorySource sourceType, String contentType) {
        return new SaveVectorMemoryCommand(
                userId, sourceType, "1", "source", contentType, "content", null, null, Map.of());
    }

    private ChatReply.GeneratedChallenge challenge(String title, String description) {
        return new ChatReply.GeneratedChallenge(title, description);
    }

    private SuggestedChatAction fullAction() {
        return new SuggestedChatAction(
                ActivityType.DIARY, 2L, "Diario emocional", "Ordenar pensamientos", "/api/activities", 50L);
    }

    private SuggestedChatAction emptyAction() {
        return new SuggestedChatAction(null, null, null, null, null, null);
    }

    private UserVectorMemoryService.PersonalitySummaryDto dto(String summary, String accepted, String rejected) {
        return new UserVectorMemoryService.PersonalitySummaryDto(summary, accepted, rejected);
    }

    // --- act ---
    private List<VectorMemory> findAcrossAllSources(Long userId, String query) {
        return service.findRelevantUserMemories(userId, query);
    }

    private List<VectorMemory> findInSource(Long userId, VectorMemorySource sourceType, String query) {
        return service.findRelevantUserMemories(userId, sourceType, query);
    }

    private List<VectorMemory> findAcrossSources(Long userId, List<VectorMemorySource> sourceTypes, String query) {
        return service.findRelevantUserMemoriesBySources(userId, sourceTypes, query);
    }

    private void saveMemory(SaveVectorMemoryCommand command) {
        service.saveMemory(command);
    }

    private void deletePersonalitySummary(Long userId) {
        service.deletePersonalitySummary(userId);
    }

    private List<String> getAllMemoryContents(Long userId) {
        return service.getAllMemoryContents(userId);
    }

    private void rememberChatMessage(Long userId, String conversationId, String message) {
        service.rememberChatMessage(userId, conversationId, message);
    }

    private void rememberGeneratedChallenge(Long userId, String conversationId, ChatReply.GeneratedChallenge challenge) {
        service.rememberGeneratedChallenge(userId, conversationId, challenge);
    }

    private void rememberRecommendedActivity(Long userId, String conversationId, Long emotionalEventId,
                                             SuggestedChatAction action) {
        service.rememberRecommendedActivity(userId, conversationId, emotionalEventId, action);
    }

    private void rememberChallengeDecision(Long userId, String conversationId, String title, String description,
                                           String decision) {
        service.rememberChallengeDecision(userId, conversationId, title, description, decision);
    }

    // --- assert ---
    private void thenMultiSourceQueryTargetsAllSources() {
        assertThat(vectorMemoryPort.lastMultiSourceQuery).isNotNull();
        assertThat(vectorMemoryPort.lastMultiSourceQuery.sourceTypes()).containsExactly(
                VectorMemorySource.CHATBOT,
                VectorMemorySource.GUIDED_LANTERNS,
                VectorMemorySource.EMOTIONAL_JOURNAL,
                VectorMemorySource.ONBOARDING);
    }

    private void thenMultiSourceIncludesChatbot() {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.sourceTypes()).contains(VectorMemorySource.CHATBOT);
    }

    private void thenMultiSourceUserIdIs(Long userId) {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.userId()).isEqualTo(userId);
    }

    private void thenMultiSourceLimitIs(Integer limit) {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.limit()).isEqualTo(limit);
    }

    private void thenMultiSourceThresholdIs(Double threshold) {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.similarityThreshold()).isEqualTo(threshold);
    }

    private void thenMultiSourceThresholdIsZero() {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.similarityThreshold()).isZero();
    }

    private void thenMultiSourceQueryContains(String fragment) {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.query()).contains(fragment);
    }

    private void thenMultiSourceQueryIs(String expected) {
        assertThat(vectorMemoryPort.lastMultiSourceQuery.query()).isEqualTo(expected);
    }

    private void thenSingleSourceIs(VectorMemorySource sourceType) {
        assertThat(vectorMemoryPort.lastSingleSourceQuery).isNotNull();
        assertThat(vectorMemoryPort.lastSingleSourceQuery.sourceType()).isEqualTo(sourceType);
    }

    private void thenContentsAre(List<VectorMemory> result, String... contents) {
        assertThat(result).extracting(VectorMemory::content).containsExactly(contents);
    }

    private void thenUserIdsAre(List<VectorMemory> result, Long... userIds) {
        assertThat(result).extracting(VectorMemory::userId).containsOnly(userIds);
    }

    private void thenTopScoreIs(List<VectorMemory> result, double score) {
        assertThat(result.get(0).score()).isEqualTo(score);
    }

    private void thenEmptyMemories(List<VectorMemory> result) {
        assertThat(result).isEmpty();
    }

    private void thenContentsExactly(List<String> contents, String... expected) {
        assertThat(contents).containsExactly(expected);
    }

    private void thenEmptyContents(List<String> contents) {
        assertThat(contents).isEmpty();
    }

    private void thenSavedCommandsEmpty() {
        assertThat(vectorMemoryPort.savedCommands).isEmpty();
    }

    private void thenSavedCommandsHasSize(int size) {
        assertThat(vectorMemoryPort.savedCommands).hasSize(size);
    }

    private void thenLastSavedSourceIdIs(String sourceId) {
        assertThat(vectorMemoryPort.savedCommands.get(0).sourceId()).isEqualTo(sourceId);
    }

    private void thenLastSavedSourceIdIsNull() {
        assertThat(vectorMemoryPort.savedCommands.get(0).sourceId()).isNull();
    }

    private void thenLastSavedSourceIdContains(String fragment) {
        assertThat(vectorMemoryPort.savedCommands.get(0).sourceId()).contains(fragment);
    }

    private void thenLastSavedContentTypeIs(String contentType) {
        assertThat(vectorMemoryPort.savedCommands.get(0).contentType()).isEqualTo(contentType);
    }

    private void thenLastSavedSourceTypeIs(VectorMemorySource sourceType) {
        assertThat(vectorMemoryPort.savedCommands.get(0).sourceType()).isEqualTo(sourceType);
    }

    private void thenSavingDoesNotThrow(SaveVectorMemoryCommand command) {
        assertThatCode(() -> service.saveMemory(command)).doesNotThrowAnyException();
    }

    private void thenDeletingDoesNotThrow(Long userId) {
        assertThatCode(() -> service.deletePersonalitySummary(userId)).doesNotThrowAnyException();
    }

    private void thenDeletedUserIdIs(Long userId) {
        assertThat(personalitySummaryRepository.deletedUserId).isEqualTo(userId);
    }

    private void thenSavedSummaryEventuallyMatches(String summary, String accepted, String rejected) {
        awaitSavedSummary();
        assertThat(personalitySummaryRepository.savedSummary).isNotNull();
        assertThat(personalitySummaryRepository.savedSummary.getSummary()).isEqualTo(summary);
        assertThat(personalitySummaryRepository.savedSummary.getAccepted()).isEqualTo(accepted);
        assertThat(personalitySummaryRepository.savedSummary.getRejected()).isEqualTo(rejected);
    }

    private void thenSavedSummaryEventuallyContains(String fragment) {
        awaitSavedSummary();
        assertThat(personalitySummaryRepository.savedSummary).isNotNull();
        assertThat(personalitySummaryRepository.savedSummary.getSummary()).contains(fragment);
    }

    private void thenSummaryNotSaved() {
        sleepQuietly(200);
        assertThat(personalitySummaryRepository.savedSummary).isNull();
    }

    private void awaitSavedSummary() {
        long start = System.currentTimeMillis();
        while (personalitySummaryRepository.savedSummary == null && (System.currentTimeMillis() - start) < 3000) {
            sleepQuietly(50);
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
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
