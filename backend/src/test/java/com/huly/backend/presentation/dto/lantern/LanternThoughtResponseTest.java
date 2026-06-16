package com.huly.backend.presentation.dto.lantern;

import com.huly.backend.infrastructure.presentation.dto.lantern.LanternThoughtResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LanternThoughtResponseTest {

    @Test
    void shouldExposeAllFields() {
        Instant now = Instant.parse("2025-06-01T10:00:00Z");
        LanternThoughtResponse response = new LanternThoughtResponse(1L, "pensamiento", false, now);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.text()).isEqualTo("pensamiento");
        assertThat(response.workedOn()).isFalse();
        assertThat(response.createdAt()).isEqualTo(now);
    }

    @Test
    void shouldHandleWorkedOnTrue() {
        LanternThoughtResponse response = new LanternThoughtResponse(2L, "trabajado", true, Instant.now());
        assertThat(response.workedOn()).isTrue();
    }

    @Test
    void shouldHandleNullCreatedAt() {
        LanternThoughtResponse response = new LanternThoughtResponse(3L, "pensamiento", false, null);
        assertThat(response.createdAt()).isNull();
    }
}
