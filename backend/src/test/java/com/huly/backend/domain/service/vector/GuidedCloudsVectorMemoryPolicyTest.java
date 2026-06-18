package com.huly.backend.domain.service.vector;

import com.huly.backend.domain.model.vector.VectorMemorySource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuidedCloudsVectorMemoryPolicyTest {

    private final GuidedCloudsVectorMemoryPolicy policy = new GuidedCloudsVectorMemoryPolicy();

    @Test
    void sourceType_shouldReturnGuidedClouds() {
        assertThat(policy.sourceType()).isEqualTo(VectorMemorySource.GUIDED_CLOUDS);
    }

    @Test
    void shouldRemember_shouldAlwaysReturnTrue() {
        assertThat(policy.shouldRemember("triste")).isTrue();
        assertThat(policy.shouldRemember("bien")).isTrue();
        assertThat(policy.shouldRemember("ab")).isTrue();
    }

    @Test
    void minContentLength_shouldReturnTwo() {
        assertThat(policy.minContentLength()).isEqualTo(2);
    }
}
