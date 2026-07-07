package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.SaveVectorMemoryCommand;
import com.huly.backend.domain.model.vector.SearchVectorMemoryQuery;
import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorMemoryPolicyTest {

    private VectorMemoryProperties properties;
    private VectorMemoryPolicy policy;

    @BeforeEach
    void setUp() {
        properties = new VectorMemoryProperties();
        policy = new VectorMemoryPolicy(properties);
    }

    @Test
    @DisplayName("Recorta y colapsa espacios al normalizar el contenido")
    void normalizeContentShouldTrimAndCollapseSpaces() {
        String result = normalize("  hola   mundo   ");

        thenNormalizedIs(result, "hola mundo");
    }

    @Test
    @DisplayName("Devuelve cadena vacía al normalizar contenido nulo")
    void normalizeContentShouldReturnEmptyWhenNull() {
        String result = normalize(null);

        thenNormalizedIsEmpty(result);
    }

    @Test
    @DisplayName("Trunca el contenido que excede la longitud máxima al normalizar")
    void normalizeContentShouldTruncateWhenExceedsMaxLength() {
        givenMaxContentLength(5);

        String result = normalize("abcdefgh");

        thenNormalizedIs(result, "abcde");
    }

    @Test
    @DisplayName("Descarta mensajes cortos por debajo del mínimo global de chatbot")
    void shouldRememberShouldRejectShortMessageBelowGlobalMinimumForChatbot() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", "CHAT_MESSAGE", "ab");

        Boolean result = shouldRemember(command, "ab");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Descarta mensajes triviales cortos rechazados por longitud")
    void shouldRememberShouldRejectTrivialMessages() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", "CHAT_MESSAGE", "hola");

        Boolean result = shouldRemember(command, "hola");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Descarta mensajes triviales aunque superen la longitud mínima")
    void shouldRememberShouldRejectTrivialMessageWithSufficientLength() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", "CHAT_MESSAGE", "buenas noches");

        Boolean result = shouldRemember(command, "buenas noches");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Descarta mensajes con señales sensibles")
    void shouldRememberShouldRejectSensitiveMessages() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", "CHAT_MESSAGE", "tengo un diagnostico");

        Boolean result = shouldRemember(command, "tengo un diagnostico");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Acepta mensajes de chatbot con información útil del usuario")
    void shouldRememberShouldAcceptUsefulMessages() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", "CHAT_MESSAGE", "me gusta jugar a la play");

        Boolean result = shouldRemember(command, "me gusta jugar a la play");

        thenIsTrue(result);
    }

    @Test
    @DisplayName("Evalúa señales de chatbot cuando el contentType es nulo")
    void shouldRememberShouldEvaluateSignalsWhenChatbotContentTypeIsNull() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", null, "me llamo sergio y vivo aca");

        Boolean result = shouldRemember(command, "me llamo sergio y vivo aca");

        thenIsTrue(result);
    }

    @Test
    @DisplayName("Descarta mensajes de chatbot sin ninguna señal de memoria")
    void shouldRememberShouldRejectChatbotMessageWithoutMemorySignal() {
        SaveVectorMemoryCommand command = chatbotCommand("USER_CHAT_MESSAGE", "CHAT_MESSAGE", "hoy fui a comprar pan y volvi");

        Boolean result = shouldRemember(command, "hoy fui a comprar pan y volvi");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Omite los filtros para memorias estructurales de chatbot")
    void shouldRememberShouldBypassFiltersForStructuralChatbotMemories() {
        SaveVectorMemoryCommand command = chatbotCommand(
                "CHALLENGE_DECISION", "CHALLENGE_DECISION", "El usuario rechazo el reto: Mirá a tu alrededor.");

        Boolean result = shouldRemember(command, command.content());

        thenIsTrue(result);
    }

    @Test
    @DisplayName("Acepta contenido de faroles guiados por encima del mínimo de longitud")
    void shouldRememberShouldAcceptGuidedLanternsContentAboveMinLength() {
        SaveVectorMemoryCommand command = guidedLanternCommand("me siento muy triste hoy");

        Boolean result = shouldRemember(command, "me siento muy triste hoy");

        thenIsTrue(result);
    }

    @Test
    @DisplayName("Descarta contenido de faroles guiados por debajo del mínimo de longitud")
    void shouldRememberShouldRejectGuidedLanternsContentBelowMinLength() {
        SaveVectorMemoryCommand command = guidedLanternCommand("ab");

        Boolean result = shouldRemember(command, "ab");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Acepta mensajes cortos de faroles guiados usando el mínimo específico de la fuente")
    void shouldRememberShouldAcceptShortGuidedLanternMessageUsingSourceSpecificMinimum() {
        SaveVectorMemoryCommand command = guidedLanternCommand("ansiedad");

        Boolean result = shouldRemember(command, "ansiedad");

        thenIsTrue(result);
    }

    @Test
    @DisplayName("Sigue descartando mensajes muy cortos de faroles guiados")
    void shouldRememberShouldStillRejectVeryShortGuidedLanternMessage() {
        SaveVectorMemoryCommand command = guidedLanternCommand("ok");

        Boolean result = shouldRemember(command, "ok");

        thenIsFalse(result);
    }

    @Test
    @DisplayName("Rechaza el límite inválido y la consulta en blanco al validar")
    void validateAndNormalizeQueryShouldRejectInvalidLimitAndBlankQuery() {
        SearchVectorMemoryQuery invalidLimit = searchQuery(1L, VectorMemorySource.CHATBOT, "consulta", 0, 0.65);
        SearchVectorMemoryQuery blankQuery = searchQuery(1L, VectorMemorySource.CHATBOT, "   ", 5, 0.65);

        thenValidateQueryThrowsContaining(invalidLimit, "limit must be between 1");
        thenValidateQueryThrowsWithMessage(blankQuery, "query is required");
    }

    @Test
    @DisplayName("Lanza excepción al validar comandos de guardado inválidos")
    void validateSaveCommandShouldThrowOnInvalidInputs() {
        SaveVectorMemoryCommand nullUser = saveCommand(null, VectorMemorySource.CHATBOT, "content");
        SaveVectorMemoryCommand nullSourceType = saveCommand(1L, null, "content");
        SaveVectorMemoryCommand blankContent = saveCommand(1L, VectorMemorySource.CHATBOT, "   ");

        thenValidateSaveThrowsWithMessage(null, "Vector memory command is required");
        thenValidateSaveThrowsWithMessage(nullUser, "userId is required");
        thenValidateSaveThrowsWithMessage(nullSourceType, "sourceType is required");
        thenValidateSaveThrowsWithMessage(blankContent, "content is required");
    }

    @Test
    @DisplayName("Lanza excepción al validar consultas de búsqueda inválidas")
    void validateAndNormalizeQueryShouldThrowOnInvalidInputs() {
        SearchVectorMemoryQuery nullUser = searchQuery(null, VectorMemorySource.CHATBOT, "query", 5, 0.5);
        SearchVectorMemoryQuery nullSource = searchQuery(1L, null, "query", 5, 0.5);
        SearchVectorMemoryQuery nullLimit = searchQuery(1L, VectorMemorySource.CHATBOT, "query", null, 0.5);
        SearchVectorMemoryQuery highLimit = searchQuery(1L, VectorMemorySource.CHATBOT, "query", 100, 0.5);
        SearchVectorMemoryQuery nullThreshold = searchQuery(1L, VectorMemorySource.CHATBOT, "query", 5, null);
        SearchVectorMemoryQuery lowThreshold = searchQuery(1L, VectorMemorySource.CHATBOT, "query", 5, -0.1);
        SearchVectorMemoryQuery highThreshold = searchQuery(1L, VectorMemorySource.CHATBOT, "query", 5, 1.1);

        thenValidateQueryThrowsWithMessage(null, "Vector memory query is required");
        thenValidateQueryThrowsWithMessage(nullUser, "userId is required");
        thenValidateQueryThrowsWithMessage(nullSource, "sourceType is required");
        thenValidateQueryThrowsContaining(nullLimit, "limit must be between");
        thenValidateQueryThrowsContaining(highLimit, "limit must be between");
        thenValidateQueryThrowsContaining(nullThreshold, "similarityThreshold must be between");
        thenValidateQueryThrowsContaining(lowThreshold, "similarityThreshold must be between");
        thenValidateQueryThrowsContaining(highThreshold, "similarityThreshold must be between");
    }

    @Test
    @DisplayName("No lanza al validar un comando de guardado válido")
    void validateSaveCommandShouldNotThrowWhenCommandIsValid() {
        SaveVectorMemoryCommand valid = saveCommand(1L, VectorMemorySource.CHATBOT, "contenido valido para guardar");

        thenSaveCommandDoesNotThrow(valid);
    }

    @Test
    @DisplayName("Devuelve la consulta normalizada al validar una búsqueda válida")
    void validateAndNormalizeQueryShouldReturnNormalizedWhenValid() {
        SearchVectorMemoryQuery valid = searchQuery(1L, VectorMemorySource.CHATBOT, "  mi   consulta  ", 5, 0.65);

        String result = validateAndNormalizeQuery(valid);

        thenNormalizedIs(result, "mi consulta");
    }

    // --- arrange ---
    private void givenMaxContentLength(int maxContentLength) {
        properties.setMaxContentLength(maxContentLength);
    }

    private SaveVectorMemoryCommand chatbotCommand(String source, String contentType, String content) {
        return new SaveVectorMemoryCommand(
                1L, VectorMemorySource.CHATBOT, "conv-1", source, contentType, content, "conv-1", null, null);
    }

    private SaveVectorMemoryCommand guidedLanternCommand(String content) {
        return new SaveVectorMemoryCommand(
                1L, VectorMemorySource.GUIDED_LANTERNS, null, null, null, content, null, null, null);
    }

    private SaveVectorMemoryCommand saveCommand(Long userId, VectorMemorySource sourceType, String content) {
        return new SaveVectorMemoryCommand(userId, sourceType, "id", "src", "type", content, null, null, null);
    }

    private SearchVectorMemoryQuery searchQuery(Long userId, VectorMemorySource sourceType, String query,
                                                Integer limit, Double similarityThreshold) {
        return new SearchVectorMemoryQuery(userId, sourceType, query, limit, similarityThreshold);
    }

    // --- act ---
    private String normalize(String content) {
        return policy.normalizeContent(content);
    }

    private Boolean shouldRemember(SaveVectorMemoryCommand command, String content) {
        return policy.shouldRemember(command, content);
    }

    private String validateAndNormalizeQuery(SearchVectorMemoryQuery query) {
        return policy.validateAndNormalizeQuery(query);
    }

    // --- assert ---
    private void thenNormalizedIs(String result, String expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenNormalizedIsEmpty(String result) {
        assertThat(result).isEmpty();
    }

    private void thenIsTrue(Boolean result) {
        assertThat(result).isTrue();
    }

    private void thenIsFalse(Boolean result) {
        assertThat(result).isFalse();
    }

    private void thenSaveCommandDoesNotThrow(SaveVectorMemoryCommand command) {
        assertThatCode(() -> policy.validateSaveCommand(command)).doesNotThrowAnyException();
    }

    private void thenValidateSaveThrowsWithMessage(SaveVectorMemoryCommand command, String message) {
        assertThatThrownBy(() -> policy.validateSaveCommand(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private void thenValidateQueryThrowsWithMessage(SearchVectorMemoryQuery query, String message) {
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }

    private void thenValidateQueryThrowsContaining(SearchVectorMemoryQuery query, String message) {
        assertThatThrownBy(() -> policy.validateAndNormalizeQuery(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(message);
    }
}
