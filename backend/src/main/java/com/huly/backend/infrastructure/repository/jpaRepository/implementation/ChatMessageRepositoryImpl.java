package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionEntity;
import com.huly.backend.infrastructure.repository.entity.GeneratedChallengeEmbeddable;
import com.huly.backend.infrastructure.repository.entity.SuggestedActionEmbeddable;
import com.huly.backend.infrastructure.repository.mapper.ChatMessageMapper;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatMessageJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Component
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final IChatMessageJpaRepository jpa;
    private final IChatSessionJpaRepository sessionJpa;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public void saveMessage(Long sessionId, ConversationMessage message) {
        ChatSessionEntity session = sessionJpa.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        ChatMessageEntity entity = chatMessageMapper.toEntity(session, message);

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

    @Override
    public Page<ChatMessage> findByConversationIdAndUserId(String conversationId, Long userId, Pageable pageable) {
        return jpa.findByChatSessionConversationIdAndChatSessionAppUserId(conversationId, userId, pageable)
                .map(chatMessageMapper::toDomain);
    }

    @Override
    public long countUserMessagesSince(Long userId, Instant since) {
        return jpa.countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(userId, MessageRole.USER, since);
    }

    @Override
    public void updateSuggestedActionDecision(Long userId, Long emotionalEventId, String decision) {
        if (userId == null || emotionalEventId == null || decision == null || decision.isBlank())
            return;


        jpa.findFirstByChatSessionAppUserIdAndRoleAndSuggestedActionEmotionalEventIdOrderByCreatedAtDesc(
                        userId,
                        MessageRole.ASSISTANT,
                        emotionalEventId
                )
                .ifPresent(entity -> {
                    SuggestedActionEmbeddable suggestedAction = entity.getSuggestedAction();
                    if (suggestedAction == null) {
                        return;
                    }
                    suggestedAction.setDecision(decision);
                    jpa.save(entity);
                });
    }

    @Override
    public void updateChallengeDecision(String conversationId, Long userId, String title, String description, String decision) {
        if (conversationId == null || conversationId.isBlank() || userId == null || title == null || title.isBlank() || decision == null || decision.isBlank())
            return;


        String normalizedDescription = description != null ? description : "";
        jpa.findFirstByChatSessionConversationIdAndChatSessionAppUserIdAndRoleAndGeneratedChallengeTitleAndGeneratedChallengeDescriptionOrderByCreatedAtDesc(
                        conversationId,
                        userId,
                        MessageRole.ASSISTANT,
                        title,
                        normalizedDescription
                )
                .ifPresent(entity -> {
                    GeneratedChallengeEmbeddable generatedChallenge = entity.getGeneratedChallenge();
                    if (generatedChallenge == null) {
                        return;
                    }
                    generatedChallenge.setDecision(decision);
                    jpa.save(entity);
                });
    }

}
