package com.huly.backend.infrastructure.repository.mapper;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import com.huly.backend.infrastructure.repository.entity.UserBadgeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
class UserBadgeMapperTest {

    private UserBadgeMapper userBadgeMapper; 

    @BeforeEach
    void setUp() {
        userBadgeMapper = new UserBadgeMapper(new BadgeMapper());
    }

    @Test 
    void toDomain_shouldMapAllFields() {
        Instant now = Instant.now();
        UserBadgeEntity entity = UserBadgeEntity.builder()
                .id(1L)
                .userId(1L)
                .badge(BadgeEntity.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build())
                .obtainedAt(now)
                .build();

        UserBadge result = userBadgeMapper.toDomain(entity);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBadge().getCode()).isEqualTo("PRIMER_PASO");
        assertThat(result.getObtainedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_shouldMapBadgeFieldsCorrectly() {
        UserBadgeEntity entity = UserBadgeEntity.builder() 
        .id(5L)
        .userId(2L)
        .badge(BadgeEntity.builder()
                .id(3L)
                .code("VALENTÍA")
                .name("Valentía")
                .description("Te animaste a enfrentar algo dificil.")
                .imageUrl("badge_valentia.webp")
                .build())
        .obtainedAt(Instant.now())
        .build();

        UserBadge result = userBadgeMapper.toDomain(entity);
        assertThat(result.getBadge().getDescription()).isEqualTo("Te animaste a enfrentar algo dificil.");
        assertThat(result.getBadge().getImageUrl()).isEqualTo("badge_valentia.webp");
    }
    
}
