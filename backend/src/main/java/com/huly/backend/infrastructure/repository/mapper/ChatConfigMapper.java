package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.ChatConfig;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import org.springframework.stereotype.Component;

@Component
public class ChatConfigMapper {

    public ChatConfigEntity toEntity(ChatConfig chatConfig) {
        return ChatConfigEntity.builder()
                .id(chatConfig.getId())
                .riskDetectionEnabled(chatConfig.getRiskDetectionEnabled())
                .systemPrompt(chatConfig.getSystemPrompt())
                .build();
    }

    public ChatConfig toDomain(ChatConfigEntity entity) {
        return ChatConfig.builder()
                .id(entity.getId())
                .riskDetectionEnabled(entity.getRiskDetectionEnabled())
                .systemPrompt(entity.getSystemPrompt())
                .build();
    }
}