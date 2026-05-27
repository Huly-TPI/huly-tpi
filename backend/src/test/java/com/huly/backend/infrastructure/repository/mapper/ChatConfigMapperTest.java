package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatConfigMapperTest {

    private final ChatConfigMapper mapper = new ChatConfigMapper();

    @Test
    void toEntity_shouldMapAllFieldsFromDomainToEntity() {
        ChatConfig domain = ChatConfig.builder()
                .id(1L)
                .riskDetectionEnabled(true)
                .systemPrompt("mi prompt")
                .build();

        ChatConfigEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getRiskDetectionEnabled()).isTrue();
        assertThat(entity.getSystemPrompt()).isEqualTo("mi prompt");
    }

    @Test
    void toEntity_shouldHandleNullFields() {
        ChatConfig domain = ChatConfig.builder().id(null).riskDetectionEnabled(null).systemPrompt(null).build();

        ChatConfigEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getRiskDetectionEnabled()).isNull();
        assertThat(entity.getSystemPrompt()).isNull();
    }

    @Test
    void toDomain_shouldMapAllFieldsFromEntityToDomain() {
        ChatConfigEntity entity = ChatConfigEntity.builder()
                .id(2L)
                .riskDetectionEnabled(false)
                .systemPrompt("prompt base")
                .build();

        ChatConfig domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isEqualTo(2L);
        assertThat(domain.getRiskDetectionEnabled()).isFalse();
        assertThat(domain.getSystemPrompt()).isEqualTo("prompt base");
    }

    @Test
    void toDomain_shouldHandleNullFields() {
        ChatConfigEntity entity = ChatConfigEntity.builder().id(null).riskDetectionEnabled(null).systemPrompt(null).build();

        ChatConfig domain = mapper.toDomain(entity);

        assertThat(domain.getId()).isNull();
        assertThat(domain.getRiskDetectionEnabled()).isNull();
        assertThat(domain.getSystemPrompt()).isNull();
    }
}
