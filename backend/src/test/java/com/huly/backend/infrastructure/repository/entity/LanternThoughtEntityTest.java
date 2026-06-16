package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.LanternStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class LanternThoughtEntityTest {

    @Test
    void builder_shouldCreateEntityWithAllFields() {
        Instant now = Instant.now();
        AppUserEntity user = AppUserEntity.builder().id(10L).build();

        LanternThoughtEntity entity = LanternThoughtEntity.builder()
                .id(1L).user(user).text("pensamiento").status(LanternStatus.ACTIVE)
                .workedOn(false).createdAt(now).build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUser()).isSameAs(user);
        assertThat(entity.getText()).isEqualTo("pensamiento");
        assertThat(entity.getStatus()).isEqualTo(LanternStatus.ACTIVE);
        assertThat(entity.isWorkedOn()).isFalse();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void setter_shouldUpdateStatus() {
        LanternThoughtEntity entity = LanternThoughtEntity.builder().status(LanternStatus.ACTIVE).build();
        entity.setStatus(LanternStatus.COMPLETED);
        assertThat(entity.getStatus()).isEqualTo(LanternStatus.COMPLETED);
    }

    @Test
    void setter_shouldUpdateWorkedOn() {
        LanternThoughtEntity entity = LanternThoughtEntity.builder().workedOn(false).build();
        entity.setWorkedOn(true);
        assertThat(entity.isWorkedOn()).isTrue();
    }

    @Test
    void noArgsConstructor_shouldCreateEntityWithNullFields() {
        LanternThoughtEntity entity = new LanternThoughtEntity();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getText()).isNull();
        assertThat(entity.getStatus()).isNull();
    }
}
