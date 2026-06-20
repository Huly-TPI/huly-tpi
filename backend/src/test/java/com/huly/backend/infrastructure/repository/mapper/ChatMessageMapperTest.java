package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.chat.SuggestedChatAction;
import com.huly.backend.domain.model.enums.ActivityType;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageMapperTest {

    private ChatMessageMapper chatMessageMapper;

    @BeforeEach
    void setUp() {
        chatMessageMapper = new ChatMessageMapper();
    }

    @Test
    void toEntity_shouldMapSuggestedActionAndChallenge() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(7L).build();
        ConversationMessage message = new ConversationMessage(
                MessageRole.ASSISTANT,
                "mensaje",
                null,
                false,
                null,
                new SuggestedChatAction(ActivityType.RESPIRACION, 4L, "Respirar", "Desc", "/guided-breathing", 12L),
                new ChatReply.GeneratedChallenge("Reto", "Descripcion"),
                "ACCEPTED",
                "REJECTED"
        );

        ChatMessageEntity entity = chatMessageMapper.toEntity(session, message);

        assertThat(entity.getChatSession()).isEqualTo(session);
        assertThat(entity.getSuggestedAction()).isNotNull();
        assertThat(entity.getSuggestedAction().getType()).isEqualTo("RESPIRACION");
        assertThat(entity.getSuggestedAction().getDecision()).isEqualTo("ACCEPTED");
        assertThat(entity.getGeneratedChallenge()).isNotNull();
        assertThat(entity.getGeneratedChallenge().getTitle()).isEqualTo("Reto");
        assertThat(entity.getGeneratedChallenge().getDecision()).isEqualTo("REJECTED");
    }

    @Test
    void toDomain_shouldMapEmbeddablesAndEmotion() {
        Instant now = Instant.now();
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(1L)
                .role(MessageRole.ASSISTANT)
                .content("contenido")
                .riskDetected(true)
                .createdAt(now)
                .suggestedAction(com.huly.backend.infrastructure.repository.entity.SuggestedActionEmbeddable.builder()
                        .type("RESPIRACION")
                        .activityId(4L)
                        .title("Respirar")
                        .description("Desc")
                        .actionUrl("/guided-breathing")
                        .emotionalEventId(12L)
                        .decision("ACCEPTED")
                        .build())
                .generatedChallenge(com.huly.backend.infrastructure.repository.entity.GeneratedChallengeEmbeddable.builder()
                        .title("Reto")
                        .description("Descripcion")
                        .decision("REJECTED")
                        .build())
                .emotions(List.of(EmotionEntity.builder().emotionDetected(EmotionType.CALM).build()))
                .build();

        ChatMessage chatMessage = chatMessageMapper.toDomain(entity);

        assertThat(chatMessage.id()).isEqualTo(1L);
        assertThat(chatMessage.detectedEmotion()).isEqualTo(EmotionType.CALM);
        assertThat(chatMessage.suggestedAction()).isNotNull();
        assertThat(chatMessage.suggestedAction().type()).isEqualTo(ActivityType.RESPIRACION);
        assertThat(chatMessage.suggestedActionDecision()).isEqualTo("ACCEPTED");
        assertThat(chatMessage.generatedChallenge()).isNotNull();
        assertThat(chatMessage.generatedChallenge().title()).isEqualTo("Reto");
        assertThat(chatMessage.challengeDecision()).isEqualTo("REJECTED");
    }
}
