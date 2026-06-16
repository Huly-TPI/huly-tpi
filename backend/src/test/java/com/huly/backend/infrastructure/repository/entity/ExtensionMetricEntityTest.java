package com.huly.backend.infrastructure.repository.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class ExtensionMetricEntityTest {

    @Test
    void testGettersAndSetters() {
        AppUserEntity user = AppUserEntity.builder().id(1L).build();
        Instant now = Instant.now();
        ExtensionMetricEntity entity = new ExtensionMetricEntity();
        
        entity.setId(10L);
        entity.setAppUser(user);
        entity.setDomain("google.com");
        entity.setActiveSeconds(120);
        entity.setScrollCount(50);
        entity.setModalsShown(2);
        entity.setRedirects(1);
        entity.setCreatedAt(now);

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getAppUser()).isEqualTo(user);
        assertThat(entity.getDomain()).isEqualTo("google.com");
        assertThat(entity.getActiveSeconds()).isEqualTo(120);
        assertThat(entity.getScrollCount()).isEqualTo(50);
        assertThat(entity.getModalsShown()).isEqualTo(2);
        assertThat(entity.getRedirects()).isEqualTo(1);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testPrePersist() {
        ExtensionMetricEntity entity = new ExtensionMetricEntity();
        assertThat(entity.getCreatedAt()).isNull();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void testAllArgsConstructorAndBuilder() {
        AppUserEntity user = AppUserEntity.builder().id(2L).build();
        Instant now = Instant.now();
        ExtensionMetricEntity entity = ExtensionMetricEntity.builder()
                .id(1L)
                .appUser(user)
                .domain("youtube.com")
                .activeSeconds(600)
                .scrollCount(300)
                .modalsShown(4)
                .redirects(3)
                .createdAt(now)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getAppUser()).isEqualTo(user);
        assertThat(entity.getDomain()).isEqualTo("youtube.com");
        assertThat(entity.getActiveSeconds()).isEqualTo(600);
        assertThat(entity.getScrollCount()).isEqualTo(300);
        assertThat(entity.getModalsShown()).isEqualTo(4);
        assertThat(entity.getRedirects()).isEqualTo(3);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }
}
