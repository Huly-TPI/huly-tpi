package com.huly.backend.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanternStatusTest {

    @Test
    void shouldHaveExactlyThreeValues() {
        assertThat(LanternStatus.values()).hasSize(3);
    }

    @Test
    void valueOf_shouldReturnCorrectStatus_forAllValues() {
        assertThat(LanternStatus.valueOf("ACTIVE")).isEqualTo(LanternStatus.ACTIVE);
        assertThat(LanternStatus.valueOf("COMPLETED")).isEqualTo(LanternStatus.COMPLETED);
        assertThat(LanternStatus.valueOf("CANCELLED")).isEqualTo(LanternStatus.CANCELLED);
    }

    @Test
    void valueOf_shouldThrow_whenValueIsInvalid() {
        assertThatThrownBy(() -> LanternStatus.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
