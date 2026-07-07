package com.huly.backend.domain.useCase.journal;

import com.huly.backend.domain.dto.journal.CreateJournalEntryRequest;
import com.huly.backend.domain.dto.journal.CreateJournalEntryResponse;
import com.huly.backend.domain.mapper.journal.CreateJournalEntryMapper;
import com.huly.backend.domain.model.enums.Mood;
import com.huly.backend.domain.model.journal.JournalEntry;
import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.repository.journal.JournalEntryRepository;
import com.huly.backend.domain.service.vector.UserVectorMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateJournalEntryUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long SAVED_ID = 10L;
    private static final Long OTHER_SAVED_ID = 5L;
    private static final Long JOURNAL_ID = 1L;
    private static final Instant CREATED_AT = Instant.parse("2026-07-06T10:00:00Z");

    private static final String CONTENT_HAPPY = "Hoy me sentí bien";
    private static final String OTHER_CONTENT = "Entrada de prueba";
    private static final String PLAIN_CONTENT = "Texto plano sin formato JSON";
    private static final String JSON_CONTENT =
            "{\"adentro\":\"Lo de adentro\",\"pensamiento\":\"Mi pensamiento\",\"bien\":\"Algo bien\",\"manana\":\"Para mañana\"}";
    private static final String PARTIAL_JSON_CONTENT =
            "{\"adentro\":\"Lo de adentro\",\"bien\":\"Algo bien\"}";
    private static final String BLANK_FIELD_JSON_CONTENT =
            "{\"adentro\":\"   \",\"bien\":\"Algo bien\"}";

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private UserVectorMemoryService userVectorMemoryService;

    private CreateJournalEntryUseCase createJournalEntryUseCase;

    private CreateJournalEntryRequest request;
    private JournalEntry savedEntry;

    @BeforeEach
    void setUp() {
        createJournalEntryUseCase = new CreateJournalEntryUseCase(
                journalEntryRepository, userVectorMemoryService, new CreateJournalEntryMapper());
    }

    @Test
    @DisplayName("Delega el guardado de la entrada al repositorio")
    void executeShouldDelegateToRepository() {
        // --- arrange ---
        givenRequest(USER_ID, CONTENT_HAPPY, Mood.HAPPY, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenRepositorySavedEntry(USER_ID, CONTENT_HAPPY, Mood.HAPPY);
    }

    @Test
    @DisplayName("Devuelve la entrada que retorna el repositorio")
    void executeShouldReturnWhatRepositoryReturns() {
        // --- arrange ---
        givenRequest(USER_ID, CONTENT_HAPPY, Mood.CALM, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        CreateJournalEntryResponse result = create();

        // --- assert ---
        thenResponseMatchesSavedEntry(result);
    }

    @Test
    @DisplayName("Pasa los argumentos correctos al repositorio")
    void executeShouldPassCorrectArgumentsToRepository() {
        // --- arrange ---
        givenRequest(OTHER_USER_ID, OTHER_CONTENT, Mood.ANXIOUS, true);
        givenRepositorySavesEntryWithId(OTHER_SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenRepositoryReceivedSaveArguments(OTHER_USER_ID, OTHER_CONTENT, Mood.ANXIOUS);
    }

    @Test
    @DisplayName("Permite un estado de ánimo nulo")
    void executeShouldAllowNullMood() {
        // --- arrange ---
        givenRequest(USER_ID, "Sin mood", null, true);
        givenRepositorySavesEntryWithId(USER_ID);

        // --- act ---
        CreateJournalEntryResponse result = create();

        // --- assert ---
        thenResponseMoodIsNull(result);
        thenRepositorySavedEntry(USER_ID, "Sin mood", null);
    }

    @Test
    @DisplayName("Guarda la memoria vectorial con el usuario y el sourceId luego de persistir")
    void executeShouldRememberJournalEntryAfterSaving() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, Mood.HAPPY, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorMemorySavedWithUserAndSourceId(USER_ID, "10");
    }

    @Test
    @DisplayName("Incluye el estado de ánimo en el contenido vectorial")
    void executeShouldIncludeMoodInVectorContent() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, Mood.HAPPY, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentContains("HAPPY");
    }

    @Test
    @DisplayName("Incluye todos los campos del diario en el contenido vectorial")
    void executeShouldIncludeAllJournalFieldsInVectorContent() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, null, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentContainsAllJournalFields();
    }

    @Test
    @DisplayName("Usa el texto plano como respaldo cuando el contenido no es JSON")
    void executeShouldUsePlainTextFallbackWhenContentIsNotJson() {
        // --- arrange ---
        givenRequest(USER_ID, PLAIN_CONTENT, null, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentContains(PLAIN_CONTENT);
    }

    @Test
    @DisplayName("Omite el texto del diario del contenido vectorial cuando useTextForAI es falso")
    void executeShouldOmitTextFromVectorContentWhenUseTextForAiIsFalse() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, Mood.HAPPY, false);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentDoesNotContainJournalFields();
    }

    @Test
    @DisplayName("Incluye el estado de ánimo aunque useTextForAI sea falso")
    void executeShouldIncludeMoodInVectorContentWhenUseTextForAiIsFalse() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, Mood.CALM, false);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentContains("CALM");
    }

    @Test
    @DisplayName("Omite el estado de ánimo del contenido vectorial cuando es nulo")
    void executeShouldOmitMoodFromVectorContentWhenMoodIsNull() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, null, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentDoesNotContain("Estado de ánimo");
    }

    @Test
    @DisplayName("Usa un sourceId nulo cuando la entrada guardada no tiene id")
    void executeShouldUseNullSourceIdWhenSavedEntryHasNoId() {
        // --- arrange ---
        givenRequest(USER_ID, JSON_CONTENT, Mood.HAPPY, true);
        givenRepositorySavesEntryWithId(null);

        // --- act ---
        create();

        // --- assert ---
        thenVectorMemorySourceIdIsNull();
    }

    @Test
    @DisplayName("Omite los campos ausentes del diario en el contenido vectorial")
    void executeShouldOmitAbsentJournalFieldsFromVectorContent() {
        // --- arrange ---
        givenRequest(USER_ID, PARTIAL_JSON_CONTENT, null, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentIncludesPresentAndOmitsAbsentFields();
    }

    @Test
    @DisplayName("Omite los campos en blanco del diario en el contenido vectorial")
    void executeShouldOmitBlankJournalFieldsFromVectorContent() {
        // --- arrange ---
        givenRequest(USER_ID, BLANK_FIELD_JSON_CONTENT, null, true);
        givenRepositorySavesEntryWithId(SAVED_ID);

        // --- act ---
        create();

        // --- assert ---
        thenVectorContentIncludesPresentAndOmitsBlankFields();
    }

    // --- arrange ---

    private void givenRequest(Long userId, String content, Mood mood, boolean useTextForAI) {
        request = new CreateJournalEntryRequest(userId, content, mood, useTextForAI);
    }

    private void givenRepositorySavesEntryWithId(Long savedId) {
        savedEntry = buildEntry(savedId, request.content(), request.mood());
        when(journalEntryRepository.save(request.userId(), request.content(), request.mood()))
                .thenReturn(savedEntry);
    }

    private JournalEntry buildEntry(Long id, String content, Mood mood) {
        return JournalEntry.builder()
                .id(id)
                .userId(request.userId())
                .journalId(JOURNAL_ID)
                .content(content)
                .mood(mood)
                .createdAt(CREATED_AT)
                .build();
    }

    // --- act ---

    private CreateJournalEntryResponse create() {
        return createJournalEntryUseCase.execute(request);
    }

    // --- assert ---

    private void thenRepositorySavedEntry(Long userId, String content, Mood mood) {
        verify(journalEntryRepository).save(userId, content, mood);
    }

    private void thenRepositoryReceivedSaveArguments(Long userId, String content, Mood mood) {
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Mood> moodCaptor = ArgumentCaptor.forClass(Mood.class);
        verify(journalEntryRepository).save(userIdCaptor.capture(), contentCaptor.capture(), moodCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(userId);
        assertThat(contentCaptor.getValue()).isEqualTo(content);
        assertThat(moodCaptor.getValue()).isEqualTo(mood);
    }

    private void thenResponseMatchesSavedEntry(CreateJournalEntryResponse result) {
        assertThat(result.id()).isEqualTo(savedEntry.getId());
        assertThat(result.content()).isEqualTo(savedEntry.getContent());
        assertThat(result.mood()).isEqualTo(savedEntry.getMood());
        assertThat(result.createdAt()).isEqualTo(savedEntry.getCreatedAt());
    }

    private void thenResponseMoodIsNull(CreateJournalEntryResponse result) {
        assertThat(result.mood()).isNull();
    }

    private void thenVectorMemorySavedWithUserAndSourceId(Long userId, String sourceId) {
        SaveVectorMemoryCommand command = captureSavedMemoryCommand();
        assertThat(command.userId()).isEqualTo(userId);
        assertThat(command.sourceId()).isEqualTo(sourceId);
    }

    private void thenVectorMemorySourceIdIsNull() {
        SaveVectorMemoryCommand command = captureSavedMemoryCommand();
        assertThat(command.sourceId()).isNull();
        assertThat(command.messageId()).isNull();
    }

    private void thenVectorContentContains(String expected) {
        assertThat(captureSavedMemoryCommand().content()).contains(expected);
    }

    private void thenVectorContentDoesNotContain(String unexpected) {
        assertThat(captureSavedMemoryCommand().content()).doesNotContain(unexpected);
    }

    private void thenVectorContentContainsAllJournalFields() {
        String content = captureSavedMemoryCommand().content();
        assertThat(content).contains("Lo de adentro");
        assertThat(content).contains("Mi pensamiento");
        assertThat(content).contains("Algo bien");
        assertThat(content).contains("Para mañana");
    }

    private void thenVectorContentDoesNotContainJournalFields() {
        String content = captureSavedMemoryCommand().content();
        assertThat(content).doesNotContain("Lo de adentro");
        assertThat(content).doesNotContain("Mi pensamiento");
        assertThat(content).doesNotContain("Algo bien");
        assertThat(content).doesNotContain("Para mañana");
    }

    private void thenVectorContentIncludesPresentAndOmitsAbsentFields() {
        String content = captureSavedMemoryCommand().content();
        assertThat(content).contains("Lo de adentro");
        assertThat(content).contains("Algo bien");
        assertThat(content).doesNotContain("Mi pensamiento");
        assertThat(content).doesNotContain("Para mañana");
    }

    private void thenVectorContentIncludesPresentAndOmitsBlankFields() {
        String content = captureSavedMemoryCommand().content();
        assertThat(content).contains("Algo bien");
        assertThat(content).doesNotContain("Lo que pasa adentro");
    }

    private SaveVectorMemoryCommand captureSavedMemoryCommand() {
        ArgumentCaptor<SaveVectorMemoryCommand> captor = ArgumentCaptor.forClass(SaveVectorMemoryCommand.class);
        verify(userVectorMemoryService).saveMemory(captor.capture());
        return captor.getValue();
    }
}
