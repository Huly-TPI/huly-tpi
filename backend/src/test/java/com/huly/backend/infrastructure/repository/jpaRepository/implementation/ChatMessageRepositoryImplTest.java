package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionEntity;
import com.huly.backend.infrastructure.repository.mapper.ChatMessageMapper;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatMessageJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageRepositoryImplTest {

    @Mock private IChatMessageJpaRepository jpa;
    @Mock private IChatSessionJpaRepository sessionJpa;
    @Mock private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatMessageRepositoryImpl repository;

    // ── saveMessage ──────────────────────────────────────────────────────────

    @Test
    void saveMessage_shouldPersistEntityWithoutEmotion_whenEmotionIsNull() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(1L).conversationId("conv-1").build();
        when(sessionJpa.findById(1L)).thenReturn(Optional.of(session));
        when(chatMessageMapper.toEntity(any(ChatSessionEntity.class), any(ConversationMessage.class)))
                .thenAnswer(invocation -> ChatMessageEntity.builder()
                        .chatSession(invocation.getArgument(0))
                        .role(invocation.<ConversationMessage>getArgument(1).role())
                        .content(invocation.<ConversationMessage>getArgument(1).content())
                        .riskDetected(invocation.<ConversationMessage>getArgument(1).riskDetected())
                        .build());

        ConversationMessage msg = new ConversationMessage(MessageRole.USER, "hola", null, false, null, null, null, null, null);
        repository.saveMessage(1L, msg);

        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getEmotions()).isNull();
        assertThat(captor.getValue().getContent()).isEqualTo("hola");
        assertThat(captor.getValue().getRole()).isEqualTo(MessageRole.USER);
    }

    @Test
    void saveMessage_shouldPersistEntityWithEmotion_whenEmotionIsPresent() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(1L).conversationId("conv-1").build();
        when(sessionJpa.findById(1L)).thenReturn(Optional.of(session));
        when(chatMessageMapper.toEntity(any(ChatSessionEntity.class), any(ConversationMessage.class)))
                .thenAnswer(invocation -> ChatMessageEntity.builder()
                        .chatSession(invocation.getArgument(0))
                        .role(invocation.<ConversationMessage>getArgument(1).role())
                        .content(invocation.<ConversationMessage>getArgument(1).content())
                        .riskDetected(invocation.<ConversationMessage>getArgument(1).riskDetected())
                        .build());

        ConversationMessage msg = new ConversationMessage(MessageRole.ASSISTANT, "resp", EmotionType.JOY, false, null, null, null, null, null);
        repository.saveMessage(1L, msg);

        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getEmotions()).hasSize(1);
        assertThat(captor.getValue().getEmotions().get(0).getEmotionDetected()).isEqualTo(EmotionType.JOY);
    }

    @Test
    void saveMessage_shouldThrowIllegalArgument_whenSessionNotFound() {
        when(sessionJpa.findById(99L)).thenReturn(Optional.empty());

        ConversationMessage msg = ConversationMessage.of(MessageRole.USER, "hola");

        assertThatThrownBy(() -> repository.saveMessage(99L, msg))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    // ── findBySessionId ──────────────────────────────────────────────────────

    @Test
    void findBySessionId_shouldReturnMappedConversationMessages() {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(1L).role(MessageRole.USER).content("hola").build();
        when(jpa.findByChatSessionIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(entity));

        List<ConversationMessage> result = repository.findBySessionId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(MessageRole.USER);
        assertThat(result.get(0).content()).isEqualTo("hola");
    }

    @Test
    void findBySessionId_shouldReturnEmptyList_whenNoMessages() {
        when(jpa.findByChatSessionIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        List<ConversationMessage> result = repository.findBySessionId(1L);

        assertThat(result).isEmpty();
    }

    // ── findByConversationId ─────────────────────────────────────────────────

    @Test
    void findByConversationIdAndUserId_shouldReturnPageWithEmotion_whenEmotionsPresent() {
        EmotionEntity emotion = EmotionEntity.builder().emotionDetected(EmotionType.SADNESS).build();
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(1L).role(MessageRole.USER).content("msg").riskDetected(true)
                .createdAt(Instant.now()).emotions(List.of(emotion)).build();
        Pageable pageable = PageRequest.of(0, 10);
        when(jpa.findByChatSessionConversationIdAndChatSessionAppUserId("conv-1", 10L, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(chatMessageMapper.toDomain(entity))
                .thenReturn(new ChatMessage(1L, MessageRole.USER, "msg", true, EmotionType.SADNESS, entity.getCreatedAt(), null, null, null, null));

        Page<ChatMessage> result = repository.findByConversationIdAndUserId("conv-1", 10L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).detectedEmotion()).isEqualTo(EmotionType.SADNESS);
        assertThat(result.getContent().get(0).riskDetected()).isTrue();
    }

    @Test
    void findByConversationIdAndUserId_shouldReturnNullEmotion_whenEmotionListIsEmpty() {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(2L).role(MessageRole.ASSISTANT).content("resp")
                .createdAt(Instant.now()).emotions(List.of()).build();
        Pageable pageable = PageRequest.of(0, 10);
        when(jpa.findByChatSessionConversationIdAndChatSessionAppUserId("conv-1", 10L, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(chatMessageMapper.toDomain(entity))
                .thenReturn(new ChatMessage(2L, MessageRole.ASSISTANT, "resp", null, null, entity.getCreatedAt(), null, null, null, null));

        Page<ChatMessage> result = repository.findByConversationIdAndUserId("conv-1", 10L, pageable);

        assertThat(result.getContent().get(0).detectedEmotion()).isNull();
    }

    @Test
    void findByConversationIdAndUserId_shouldReturnNullEmotion_whenEmotionListIsNull() {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(3L).role(MessageRole.USER).content("msg")
                .createdAt(Instant.now()).emotions(null).build();
        Pageable pageable = PageRequest.of(0, 10);
        when(jpa.findByChatSessionConversationIdAndChatSessionAppUserId("conv-1", 10L, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(chatMessageMapper.toDomain(entity))
                .thenReturn(new ChatMessage(3L, MessageRole.USER, "msg", null, null, entity.getCreatedAt(), null, null, null, null));

        Page<ChatMessage> result = repository.findByConversationIdAndUserId("conv-1", 10L, pageable);

        assertThat(result.getContent().get(0).detectedEmotion()).isNull();
    }

    // ── countUserMessagesSince ───────────────────────────────────────────────

    @Test
    void countUserMessagesSince_shouldDelegateToJpaCountingUserRole() {
        Instant since = Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS);
        when(jpa.countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(10L, MessageRole.USER, since))
                .thenReturn(7L);

        long result = repository.countUserMessagesSince(10L, since);

        assertThat(result).isEqualTo(7L);
        verify(jpa).countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(10L, MessageRole.USER, since);
    }
}
