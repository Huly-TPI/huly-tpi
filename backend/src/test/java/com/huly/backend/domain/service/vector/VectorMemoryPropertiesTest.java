package com.huly.backend.domain.service.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VectorMemoryPropertiesTest {

    private final VectorMemoryProperties properties = new VectorMemoryProperties();

    @Test
    @DisplayName("Expone los valores por defecto de la memoria vectorial")
    void shouldExposeDefaultValues() {
        thenDefaultsArePresent();
    }

    @Test
    @DisplayName("Actualiza cada propiedad mediante sus setters")
    void shouldUpdateEachPropertyThroughSetters() {
        setCustomValues();

        thenCustomValuesArePresent();
    }

    // --- act ---
    private void setCustomValues() {
        properties.setMinContentLength(1);
        properties.setGuidedLanternsMinContentLength(2);
        properties.setMaxContentLength(3);
        properties.setDefaultLimit(4);
        properties.setMaxLimit(5);
        properties.setSimilarityThreshold(0.1);
        properties.setRecallSimilarityThreshold(0.2);
    }

    // --- assert ---
    private void thenDefaultsArePresent() {
        assertThat(properties.getMinContentLength()).isEqualTo(12);
        assertThat(properties.getGuidedLanternsMinContentLength()).isEqualTo(3);
        assertThat(properties.getMaxContentLength()).isEqualTo(4_000);
        assertThat(properties.getDefaultLimit()).isEqualTo(5);
        assertThat(properties.getMaxLimit()).isEqualTo(20);
        assertThat(properties.getSimilarityThreshold()).isEqualTo(0.65);
        assertThat(properties.getRecallSimilarityThreshold()).isEqualTo(0.35);
    }

    private void thenCustomValuesArePresent() {
        assertThat(properties.getMinContentLength()).isEqualTo(1);
        assertThat(properties.getGuidedLanternsMinContentLength()).isEqualTo(2);
        assertThat(properties.getMaxContentLength()).isEqualTo(3);
        assertThat(properties.getDefaultLimit()).isEqualTo(4);
        assertThat(properties.getMaxLimit()).isEqualTo(5);
        assertThat(properties.getSimilarityThreshold()).isEqualTo(0.1);
        assertThat(properties.getRecallSimilarityThreshold()).isEqualTo(0.2);
    }
}
