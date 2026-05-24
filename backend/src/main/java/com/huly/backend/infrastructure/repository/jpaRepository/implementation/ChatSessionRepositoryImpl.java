package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.repository.ChatSessionRepository;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final IChatSessionJpaRepository jpa;

    @Override
    public Optional<String> findConversationIdBySessionId(Long sessionId) {
        return jpa.findById(sessionId).map(ChatSessionEntity::getConversationId);
    }

    @Override
    public Long saveSession(String conversationId) {
        ChatSessionEntity entity = ChatSessionEntity.builder()
                .conversationId(conversationId)
                .startAt(java.time.Instant.now())
                .build();
        return jpa.save(entity).getId();
    }

    @Override
    public Optional<Long> findSessionIdByConversationId(String conversationId) {
        return jpa.findByConversationId(conversationId).map(ChatSessionEntity::getId);
    }
}