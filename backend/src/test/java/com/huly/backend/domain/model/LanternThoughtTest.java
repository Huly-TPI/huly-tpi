package com.huly.backend.domain.model;

import com.huly.backend.domain.model.enums.LanternStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LanternThoughtTest {

    @Test
    void builder_shouldCreateLanternThoughtWithAllFields() {
        Instant now = Instant.now();
        LanternThought thought = LanternThought.builder()
                .id(1L).userId(2L).text("pensamiento").status(LanternStatus.ACTIVE)
                .workedOn(false).createdAt(now).build();

        assertThat(thought.getId()).isEqualTo(1L);
        assertThat(thought.getUserId()).isEqualTo(2L);
        assertThat(thought.getText()).isEqualTo("pensamiento");
        assertThat(thought.getStatus()).isEqualTo(LanternStatus.ACTIVE);
        assertThat(thought.isWorkedOn()).isFalse();
        assertThat(thought.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setter_shouldUpdateStatus() {
        LanternThought thought = LanternThought.builder().status(LanternStatus.ACTIVE).build();
        thought.setStatus(LanternStatus.COMPLETED);
        assertThat(thought.getStatus()).isEqualTo(LanternStatus.COMPLETED);
    }

    @Test
    void setter_shouldUpdateWorkedOn() {
        LanternThought thought = LanternThought.builder().workedOn(false).build();
        thought.setWorkedOn(true);
        assertThat(thought.isWorkedOn()).isTrue();
    }

    @Test
    void noArgsConstructor_shouldCreateThoughtWithNullFields() {
        LanternThought thought = new LanternThought();
        assertThat(thought.getId()).isNull();
        assertThat(thought.getText()).isNull();
        assertThat(thought.getStatus()).isNull();
    }
}
