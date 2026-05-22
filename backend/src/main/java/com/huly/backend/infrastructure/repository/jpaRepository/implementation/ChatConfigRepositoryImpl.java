package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.ChatConfig;
import com.huly.backend.domain.repository.ChatConfigRepository;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ChatConfigRepositoryImpl implements ChatConfigRepository {

    private final IChatConfigJpaRepository jpa;

    @Override
    public Optional<ChatConfig> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public ChatConfig save(ChatConfig chatConfig) {
        ChatConfigEntity entity = toEntity(chatConfig);
        ChatConfigEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ChatConfig> findFirst() {
        return jpa.findAll().stream().findFirst().map(this::toDomain);
    }

    private ChatConfigEntity toEntity(ChatConfig chatConfig) {
        return ChatConfigEntity.builder()
                .id(chatConfig.getId())
                .riskDetectionEnabled(chatConfig.getRiskDetectionEnabled())
                .systemPrompt(chatConfig.getSystemPrompt())
                .build();
    }

    private ChatConfig toDomain(ChatConfigEntity entity) {
        return ChatConfig.builder()
                .id(entity.getId())
                .riskDetectionEnabled(entity.getRiskDetectionEnabled())
                .systemPrompt(entity.getSystemPrompt())
                .build();
    }
}
