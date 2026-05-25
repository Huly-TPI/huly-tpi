package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatMessageJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final IChatMessageJpaRepository jpa;
    private final IChatSessionJpaRepository sessionJpa;

    @Override
    public void saveMessage(Long sessionId, ConversationMessage message) {
        ChatSessionEntity session = sessionJpa.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .chatSession(session)
                .role(message.role())
                .content(message.content())
                .createdAt(Instant.now())
                .build();

        jpa.save(entity);
    }

    @Override
    public List<ConversationMessage> findBySessionId(Long sessionId) {
        return jpa.findByChatSessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(entity -> new ConversationMessage(entity.getRole(), entity.getContent()))
                .toList();
    }
}