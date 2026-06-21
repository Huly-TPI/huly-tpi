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
import com.huly.backend.infrastructure.repository.entity.GeneratedChallengeEmbeddable;
import com.huly.backend.infrastructure.repository.entity.SuggestedActionEmbeddable;
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
    void toEntity_shouldReturnNullNestedObjects_whenMessageHasNoSuggestedActionOrChallenge() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(8L).build();
        ConversationMessage message = new ConversationMessage(
                MessageRole.USER,
                "mensaje simple",
                null,
                false,
                null,
                null,
                null,
                null,
                null
        );

        ChatMessageEntity entity = chatMessageMapper.toEntity(session, message);

        assertThat(entity.getChatSession()).isEqualTo(session);
        assertThat(entity.getSuggestedAction()).isNull();
        assertThat(entity.getGeneratedChallenge()).isNull();
    }

    @Test
    void toEntity_shouldMapNullSuggestedActionTypeAndEmptyChallengeDescription() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(9L).build();
        ConversationMessage message = new ConversationMessage(
                MessageRole.ASSISTANT,
                "mensaje",
                null,
                false,
                null,
                new SuggestedChatAction(null, 4L, "Respirar", "Desc", "/guided-breathing", 12L),
                new ChatReply.GeneratedChallenge("Reto", null),
                "ACCEPTED",
                "PENDING"
        );

        ChatMessageEntity entity = chatMessageMapper.toEntity(session, message);

        assertThat(entity.getSuggestedAction()).isNotNull();
        assertThat(entity.getSuggestedAction().getType()).isNull();
        assertThat(entity.getGeneratedChallenge()).isNotNull();
        assertThat(entity.getGeneratedChallenge().getDescription()).isEmpty();
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

    @Test
    void toDomain_shouldReturnNullNestedObjects_whenEmbeddablesAreMissing() {
        Instant now = Instant.now();
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(2L)
                .role(MessageRole.USER)
                .content("contenido")
                .riskDetected(false)
                .createdAt(now)
                .suggestedAction(null)
                .generatedChallenge(null)
                .emotions(null)
                .build();

        ChatMessage chatMessage = chatMessageMapper.toDomain(entity);

        assertThat(chatMessage.id()).isEqualTo(2L);
        assertThat(chatMessage.detectedEmotion()).isNull();
        assertThat(chatMessage.suggestedAction()).isNull();
        assertThat(chatMessage.generatedChallenge()).isNull();
        assertThat(chatMessage.suggestedActionDecision()).isNull();
        assertThat(chatMessage.challengeDecision()).isNull();
    }

    @Test
    void toDomain_shouldReturnNullSuggestedAction_whenSuggestedActionTypeIsNull() {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(3L)
                .role(MessageRole.ASSISTANT)
                .content("contenido")
                .createdAt(Instant.now())
                .suggestedAction(SuggestedActionEmbeddable.builder()
                        .type(null)
                        .activityId(4L)
                        .title("Respirar")
                        .description("Desc")
                        .actionUrl("/guided-breathing")
                        .emotionalEventId(12L)
                        .decision("ACCEPTED")
                        .build())
                .build();

        ChatMessage chatMessage = chatMessageMapper.toDomain(entity);

        assertThat(chatMessage.suggestedAction()).isNull();
        assertThat(chatMessage.suggestedActionDecision()).isEqualTo("ACCEPTED");
    }

    @Test
    void toDomain_shouldReturnNullGeneratedChallenge_whenGeneratedChallengeTitleIsNull() {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(4L)
                .role(MessageRole.ASSISTANT)
                .content("contenido")
                .createdAt(Instant.now())
                .generatedChallenge(GeneratedChallengeEmbeddable.builder()
                        .title(null)
                        .description("Descripcion")
                        .decision("REJECTED")
                        .build())
                .build();

        ChatMessage chatMessage = chatMessageMapper.toDomain(entity);

        assertThat(chatMessage.generatedChallenge()).isNull();
        assertThat(chatMessage.challengeDecision()).isEqualTo("REJECTED");
    }
}
