package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionEntity;
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
                .riskDetected(message.riskDetected())
                .createdAt(Instant.now())
                .build();

        if (message.detectedEmotion() != null) {
            EmotionEntity emotion = EmotionEntity.builder()
                    .emotionDetected(message.detectedEmotion())
                    .chatMessage(entity)
                    .build();
            entity.setEmotions(List.of(emotion));
        }

        jpa.save(entity);
    }

    @Override
    public List<ConversationMessage> findBySessionId(Long sessionId) {
        return jpa.findByChatSessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(e -> ConversationMessage.of(e.getRole(), e.getContent()))
                .toList();
    }
}
