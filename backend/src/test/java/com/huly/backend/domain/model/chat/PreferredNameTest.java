package com.huly.backend.domain.model.chat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PreferredNameTest {

    @Test
    @DisplayName("Capitaliza un nombre en minúscula")
    void sanitizeShouldCapitalizeLowercaseName() {
        Optional<String> result = PreferredName.sanitize("sergito");

        assertThat(result).contains("Sergito");
    }

    @Test
    @DisplayName("Respeta la capitalización existente")
    void sanitizeShouldKeepExistingCapitalization() {
        Optional<String> result = PreferredName.sanitize("Sergito");

        assertThat(result).contains("Sergito");
    }

    @Test
    @DisplayName("Acepta nombres compuestos de hasta tres palabras")
    void sanitizeShouldAcceptCompoundName() {
        Optional<String> result = PreferredName.sanitize("juan pablo");

        assertThat(result).contains("Juan Pablo");
    }

    @Test
    @DisplayName("Recorta desde el primer signo de puntuación")
    void sanitizeShouldTruncateAtFirstPunctuation() {
        Optional<String> result = PreferredName.sanitize("Crack, gracias");

        assertThat(result).contains("Crack");
    }

    @Test
    @DisplayName("Quita el 'por favor' final")
    void sanitizeShouldStripTrailingPorFavor() {
        Optional<String> result = PreferredName.sanitize("sergio por favor");

        assertThat(result).contains("Sergio");
    }

    @Test
    @DisplayName("Quita comillas que envuelven el nombre")
    void sanitizeShouldStripSurroundingQuotes() {
        Optional<String> result = PreferredName.sanitize("\"Crack\"");

        assertThat(result).contains("Crack");
    }

    @Test
    @DisplayName("Rechaza términos que no son nombres")
    void sanitizeShouldRejectInvalidTerms() {
        assertThat(PreferredName.sanitize("hola")).isEmpty();
        assertThat(PreferredName.sanitize("gracias")).isEmpty();
    }

    @Test
    @DisplayName("Rechaza cuando la primera palabra es inválida")
    void sanitizeShouldRejectWhenFirstWordIsInvalid() {
        assertThat(PreferredName.sanitize("buenos dias")).isEmpty();
    }

    @Test
    @DisplayName("Rechaza nombres más largos que el máximo permitido")
    void sanitizeShouldRejectTooLongName() {
        assertThat(PreferredName.sanitize("a".repeat(51))).isEmpty();
    }

    @Test
    @DisplayName("Rechaza frases de más de tres palabras")
    void sanitizeShouldRejectMoreThanThreeWords() {
        assertThat(PreferredName.sanitize("Juan Carlos Perez Lopez")).isEmpty();
    }

    @Test
    @DisplayName("Rechaza texto que no empieza con una letra")
    void sanitizeShouldRejectNonLetterStart() {
        assertThat(PreferredName.sanitize("123")).isEmpty();
    }

    @Test
    @DisplayName("Rechaza null y cadenas en blanco")
    void sanitizeShouldRejectNullAndBlank() {
        assertThat(PreferredName.sanitize(null)).isEmpty();
        assertThat(PreferredName.sanitize("   ")).isEmpty();
    }
}
