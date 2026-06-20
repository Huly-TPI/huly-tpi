package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionEntity;
import com.huly.backend.infrastructure.repository.entity.GeneratedChallengeEmbeddable;
import com.huly.backend.infrastructure.repository.entity.SuggestedActionEmbeddable;
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
                .suggestedAction(toSuggestedActionEmbeddable(message))
                .generatedChallenge(toGeneratedChallengeEmbeddable(message))
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

    @Override
    public Page<ChatMessage> findByConversationIdAndUserId(String conversationId, Long userId, Pageable pageable) {
        return jpa.findByChatSessionConversationIdAndChatSessionAppUserId(conversationId, userId, pageable)
                .map(this::toChatMessage);
    }

    @Override
    public long countUserMessagesSince(Long userId, Instant since) {
        return jpa.countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(userId, MessageRole.USER, since);
    }

    @Override
    public void updateSuggestedActionDecision(Long userId, Long emotionalEventId, String decision) {
        if (userId == null || emotionalEventId == null || decision == null || decision.isBlank()) {
            return;
        }

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
        if (conversationId == null || conversationId.isBlank() || userId == null || title == null || title.isBlank() || decision == null || decision.isBlank()) {
            return;
        }

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

    private ChatMessage toChatMessage(ChatMessageEntity entity) {
        EmotionType emotion = entity.getEmotions() != null && !entity.getEmotions().isEmpty()
                ? entity.getEmotions().get(0).getEmotionDetected()
                : null;
        return new ChatMessage(
                entity.getId(),
                entity.getRole(),
                entity.getContent(),
                entity.getRiskDetected(),
                emotion,
                entity.getCreatedAt(),
                toSuggestedAction(entity),
                toGeneratedChallenge(entity),
                entity.getSuggestedAction() != null ? entity.getSuggestedAction().getDecision() : null,
                entity.getGeneratedChallenge() != null ? entity.getGeneratedChallenge().getDecision() : null
        );
    }

    private SuggestedChatAction toSuggestedAction(ChatMessageEntity entity) {
        SuggestedActionEmbeddable suggestedAction = entity.getSuggestedAction();
        if (suggestedAction == null || suggestedAction.getType() == null) {
            return null;
        }

        return new SuggestedChatAction(
                ActivityType.valueOf(suggestedAction.getType()),
                suggestedAction.getActivityId(),
                suggestedAction.getTitle(),
                suggestedAction.getDescription(),
                suggestedAction.getActionUrl(),
                suggestedAction.getEmotionalEventId()
        );
    }

    private ChatReply.GeneratedChallenge toGeneratedChallenge(ChatMessageEntity entity) {
        GeneratedChallengeEmbeddable generatedChallenge = entity.getGeneratedChallenge();
        if (generatedChallenge == null || generatedChallenge.getTitle() == null) {
            return null;
        }

        return new ChatReply.GeneratedChallenge(
                generatedChallenge.getTitle(),
                generatedChallenge.getDescription()
        );
    }

    private SuggestedActionEmbeddable toSuggestedActionEmbeddable(ConversationMessage message) {
        SuggestedChatAction suggestedAction = message.suggestedAction();
        if (suggestedAction == null) {
            return null;
        }

        return SuggestedActionEmbeddable.builder()
                .type(suggestedAction.type() != null ? suggestedAction.type().name() : null)
                .activityId(suggestedAction.activityId())
                .title(suggestedAction.title())
                .description(suggestedAction.description())
                .actionUrl(suggestedAction.actionUrl())
                .emotionalEventId(suggestedAction.emotionalEventId())
                .decision(message.suggestedActionDecision())
                .build();
    }

    private GeneratedChallengeEmbeddable toGeneratedChallengeEmbeddable(ConversationMessage message) {
        ChatReply.GeneratedChallenge generatedChallenge = message.generatedChallenge();
        if (generatedChallenge == null) {
            return null;
        }

        return GeneratedChallengeEmbeddable.builder()
                .title(generatedChallenge.title())
                .description(generatedChallenge.description() != null ? generatedChallenge.description() : "")
                .decision(message.challengeDecision())
                .build();
    }
}
