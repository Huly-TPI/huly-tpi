package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.domain.repository.chat.ChatConfigRepository;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.ChatConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ChatConfigRepositoryImpl implements ChatConfigRepository {

    private final IChatConfigJpaRepository jpa;
    private final ChatConfigMapper mapper;

    @Override
    public Optional<ChatConfig> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public ChatConfig save(ChatConfig chatConfig) {
        ChatConfigEntity entity = mapper.toEntity(chatConfig);
        ChatConfigEntity saved = jpa.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<ChatConfig> findFirst() {
        return jpa.findAll().stream().findFirst().map(mapper::toDomain);
    }
}