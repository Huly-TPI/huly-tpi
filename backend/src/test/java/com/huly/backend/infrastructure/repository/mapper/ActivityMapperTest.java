package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityMapperTest {

    private final ActivityMapper mapper = new ActivityMapper();

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        ActivityEntity entity = ActivityEntity.builder()
                .id(1L)
                .type(ActivityType.BREATHING)
                .valenceMin(-0.1).valenceMax(0.2)
                .arousalMin(-0.3).arousalMax(0.4)
                .dominanceMin(-0.5).dominanceMax(0.6)
                .effectValence(0.05).effectArousal(0.1).effectDominance(-0.1)
                .title("T").description("D")
                .goalKeywords("K").routePath("/p")
                .build();

        Activity domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getType()).isEqualTo(ActivityType.BREATHING);
        assertThat(domain.getValenceMin()).isEqualTo(-0.1);
        assertThat(domain.getValenceMax()).isEqualTo(0.2);
        assertThat(domain.getArousalMin()).isEqualTo(-0.3);
        assertThat(domain.getArousalMax()).isEqualTo(0.4);
        assertThat(domain.getDominanceMin()).isEqualTo(-0.5);
        assertThat(domain.getDominanceMax()).isEqualTo(0.6);
        assertThat(domain.getEffectValence()).isEqualTo(0.05);
        assertThat(domain.getEffectArousal()).isEqualTo(0.1);
        assertThat(domain.getEffectDominance()).isEqualTo(-0.1);
        assertThat(domain.getTitle()).isEqualTo("T");
        assertThat(domain.getDescription()).isEqualTo("D");
        assertThat(domain.getGoalKeywords()).isEqualTo("K");
        assertThat(domain.getRoutePath()).isEqualTo("/p");
    }

    @Test
    void toEntity_shouldMapAllFields() {
        Activity domain = Activity.builder()
                .id(1L)
                .type(ActivityType.BREATHING)
                .valenceMin(-0.1).valenceMax(0.2)
                .arousalMin(-0.3).arousalMax(0.4)
                .dominanceMin(-0.5).dominanceMax(0.6)
                .effectValence(0.05).effectArousal(0.1).effectDominance(-0.1)
                .title("T").description("D")
                .goalKeywords("K").routePath("/p")
                .build();

        ActivityEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getType()).isEqualTo(ActivityType.BREATHING);
        assertThat(entity.getValenceMin()).isEqualTo(-0.1);
        assertThat(entity.getValenceMax()).isEqualTo(0.2);
        assertThat(entity.getArousalMin()).isEqualTo(-0.3);
        assertThat(entity.getArousalMax()).isEqualTo(0.4);
        assertThat(entity.getDominanceMin()).isEqualTo(-0.5);
        assertThat(entity.getDominanceMax()).isEqualTo(0.6);
        assertThat(entity.getEffectValence()).isEqualTo(0.05);
        assertThat(entity.getEffectArousal()).isEqualTo(0.1);
        assertThat(entity.getEffectDominance()).isEqualTo(-0.1);
        assertThat(entity.getTitle()).isEqualTo("T");
        assertThat(entity.getDescription()).isEqualTo("D");
        assertThat(entity.getGoalKeywords()).isEqualTo("K");
        assertThat(entity.getRoutePath()).isEqualTo("/p");
    }
}
