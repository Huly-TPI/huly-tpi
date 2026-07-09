package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.GeneratedChallengeEmbeddable;
import com.huly.backend.infrastructure.repository.entity.SuggestedActionEmbeddable;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ChatMessageMapper {

    public ChatMessageEntity toEntity(ChatSessionEntity session, ConversationMessage message) {
        return ChatMessageEntity.builder()
                .chatSession(session)
                .role(message.role())
                .content(message.content())
                .riskDetected(message.riskDetected())
                .createdAt(Instant.now())
                .suggestedAction(toSuggestedActionEmbeddable(message))
                .generatedChallenge(toGeneratedChallengeEmbeddable(message))
                .build();
    }

    public ChatMessage toDomain(ChatMessageEntity entity) {
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
                toSuggestedAction(entity.getSuggestedAction()),
                toGeneratedChallenge(entity.getGeneratedChallenge()),
                entity.getSuggestedAction() != null ? entity.getSuggestedAction().getDecision() : null,
                entity.getGeneratedChallenge() != null ? entity.getGeneratedChallenge().getDecision() : null
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

    private SuggestedChatAction toSuggestedAction(SuggestedActionEmbeddable suggestedAction) {
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

    private ChatReply.GeneratedChallenge toGeneratedChallenge(GeneratedChallengeEmbeddable generatedChallenge) {
        if (generatedChallenge == null || generatedChallenge.getTitle() == null) {
            return null;
        }

        return new ChatReply.GeneratedChallenge(
                generatedChallenge.getTitle(),
                generatedChallenge.getDescription()
        );
    }

    public ConversationMessage toConversationMessage(ChatMessageEntity entity) {
        if (entity == null) {
            return null;
        }
        EmotionType emotion = entity.getEmotions() != null && !entity.getEmotions().isEmpty()
                ? entity.getEmotions().get(0).getEmotionDetected()
                : null;
        return new ConversationMessage(
                entity.getRole(),
                entity.getContent(),
                emotion,
                entity.getRiskDetected(),
                null,
                toSuggestedAction(entity.getSuggestedAction()),
                toGeneratedChallenge(entity.getGeneratedChallenge()),
                entity.getSuggestedAction() != null ? entity.getSuggestedAction().getDecision() : null,
                entity.getGeneratedChallenge() != null ? entity.getGeneratedChallenge().getDecision() : null
        );
    }
}
