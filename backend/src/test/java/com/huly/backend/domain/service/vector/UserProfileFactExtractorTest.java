package com.huly.backend.domain.service.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileFactExtractorTest {

    private final UserProfileFactExtractor extractor = new UserProfileFactExtractor();

    @Test
    @DisplayName("No extrae datos cuando el mensaje es nulo")
    void extractProfileFactsShouldReturnEmptyWhenMessageIsNull() {
        Optional<String> result = extractProfileFacts(null);

        thenNoFacts(result);
    }

    @Test
    @DisplayName("No extrae datos cuando el mensaje está en blanco")
    void extractProfileFactsShouldReturnEmptyWhenMessageIsBlank() {
        Optional<String> result = extractProfileFacts("   ");

        thenNoFacts(result);
    }

    @Test
    @DisplayName("No extrae datos cuando el mensaje no contiene información personal")
    void extractProfileFactsShouldReturnEmptyWhenNoFactPresent() {
        Optional<String> result = extractProfileFacts("hola, ¿cómo va todo hoy?");

        thenNoFacts(result);
    }

    @Test
    @DisplayName("Extrae y capitaliza el nombre del usuario")
    void extractProfileFactsShouldExtractName() {
        Optional<String> result = extractProfileFacts("me llamo ana");

        thenFactIs(result, "El usuario se llama Ana.");
    }

    @Test
    @DisplayName("Extrae la edad del usuario")
    void extractProfileFactsShouldExtractAge() {
        Optional<String> result = extractProfileFacts("tengo 25 años");

        thenFactIs(result, "El usuario tiene 25 años.");
    }

    @Test
    @DisplayName("Extrae la condición de estudiante sin carrera")
    void extractProfileFactsShouldExtractStudentWithoutField() {
        Optional<String> result = extractProfileFacts("soy estudiante");

        thenFactIs(result, "El usuario es estudiante.");
    }

    @Test
    @DisplayName("Extrae la carrera del estudiante cuando la menciona")
    void extractProfileFactsShouldExtractStudentWithField() {
        Optional<String> result = extractProfileFacts("soy estudiante de medicina");

        thenFactIs(result, "El usuario es estudiante de medicina.");
    }

    @Test
    @DisplayName("Combina nombre, edad y estudios en un solo texto")
    void extractProfileFactsShouldCombineMultipleFacts() {
        Optional<String> result = extractProfileFacts("me llamo Ana, tengo 30 años y soy estudiante de derecho");

        thenFactContains(result,
                "El usuario se llama Ana.",
                "El usuario tiene 30 años.",
                "El usuario es estudiante de derecho.");
    }

    @Test
    @DisplayName("Reconoce cada variante de consulta sobre datos personales")
    void asksForProfileFactShouldDetectEachKeyword() {
        thenRecognizes("cual es mi edad");
        thenRecognizes("cuantos anos");
        thenRecognizes("cuantos anios");
        thenRecognizes("cual es mi nombre");
        thenRecognizes("como me llamo");
        thenRecognizes("sabes quien soy");
        thenRecognizes("mi estudio");
        thenRecognizes("soy estudiante");
        thenRecognizes("mis datos personales");
        thenRecognizes("que sabes de mi");
        thenRecognizes("quiero saber de mi");
        thenRecognizes("cuentame sobre mi");
    }

    @Test
    @DisplayName("No reconoce consultas sin datos personales")
    void asksForProfileFactShouldReturnFalseWhenNoKeyword() {
        thenDoesNotRecognize("hola como estas hoy");
    }

    @Test
    @DisplayName("No reconoce consultas nulas")
    void asksForProfileFactShouldReturnFalseWhenQueryIsNull() {
        thenDoesNotRecognize(null);
    }

    @Test
    @DisplayName("Construye la consulta de recuperación anteponiendo el texto original")
    void buildProfileRecallQueryShouldPrependOriginalQuery() {
        String result = buildProfileRecallQuery("que sabes de mi");

        thenRecallQueryStartsWith(result, "que sabes de mi");
        thenRecallQueryContainsProfileTerms(result);
    }

    @Test
    @DisplayName("Construye la consulta de recuperación con consulta nula")
    void buildProfileRecallQueryShouldHandleNullQuery() {
        String result = buildProfileRecallQuery(null);

        thenRecallQueryContainsProfileTerms(result);
    }

    // --- act ---
    private Optional<String> extractProfileFacts(String message) {
        return extractor.extractProfileFacts(message);
    }

    private Boolean asksForProfileFact(String query) {
        return extractor.asksForProfileFact(query);
    }

    private String buildProfileRecallQuery(String query) {
        return extractor.buildProfileRecallQuery(query);
    }

    // --- assert ---
    private void thenNoFacts(Optional<String> result) {
        assertThat(result).isEmpty();
    }

    private void thenFactIs(Optional<String> result, String expected) {
        assertThat(result).contains(expected);
    }

    private void thenFactContains(Optional<String> result, String... fragments) {
        assertThat(result).isPresent();
        assertThat(result.get()).contains(fragments);
    }

    private void thenRecognizes(String query) {
        assertThat(asksForProfileFact(query)).isTrue();
    }

    private void thenDoesNotRecognize(String query) {
        assertThat(asksForProfileFact(query)).isFalse();
    }

    private void thenRecallQueryStartsWith(String result, String prefix) {
        assertThat(result).startsWith(prefix);
    }

    private void thenRecallQueryContainsProfileTerms(String result) {
        assertThat(result).contains("datos personales").contains("perfil");
    }
}
