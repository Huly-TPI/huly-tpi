package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.entity.EmotionEntity;
import com.huly.backend.infrastructure.repository.entity.GeneratedChallengeEmbeddable;
import com.huly.backend.infrastructure.repository.entity.SuggestedActionEmbeddable;
import com.huly.backend.infrastructure.repository.mapper.ChatMessageMapper;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatMessageJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageRepositoryImplTest {

    private static final Long SESSION_ID = 1L;
    private static final Long MISSING_SESSION_ID = 99L;
    private static final Long USER_ID = 10L;
    private static final String CONVERSATION_ID = "conv-1";
    private static final Long SUGGESTED_USER_ID = 20L;
    private static final Long EMOTIONAL_EVENT_ID = 15L;
    private static final Long CHALLENGE_USER_ID = 10L;
    private static final String TITLE = "Reto";
    private static final String DESCRIPTION = "Descripcion";
    private static final String AUDIO_PREFIX = "[Mensaje de voz transcrito]";
    private static final Instant CREATED_AT = Instant.parse("2026-02-02T08:00:00Z");
    private static final Instant SINCE = Instant.parse("2026-01-01T00:00:00Z");
    private static final Pageable PAGEABLE = PageRequest.of(0, 10);

    @Mock private IChatMessageJpaRepository jpa;
    @Mock private IChatSessionJpaRepository sessionJpa;
    @Mock private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private ChatMessageRepositoryImpl repository;

    @Test
    @DisplayName("Persiste el mensaje sin emoción cuando no hay emoción detectada")
    void saveMessageShouldPersistEntityWithoutEmotionWhenEmotionIsNull() {
        givenSession(SESSION_ID);
        givenMapperEchoesEntity();

        saveMessage(SESSION_ID, conversationMessage(MessageRole.USER, "hola", null));

        thenPersistedEntityHasNoEmotion("hola", MessageRole.USER);
    }

    @Test
    @DisplayName("Persiste el mensaje con emoción cuando hay emoción detectada")
    void saveMessageShouldPersistEntityWithEmotionWhenEmotionIsPresent() {
        givenSession(SESSION_ID);
        givenMapperEchoesEntity();

        saveMessage(SESSION_ID, conversationMessage(MessageRole.ASSISTANT, "resp", EmotionType.JOY));

        thenPersistedEntityHasEmotion(EmotionType.JOY);
    }

    @Test
    @DisplayName("Falla al guardar cuando la sesión no existe")
    void saveMessageShouldThrowIllegalArgumentWhenSessionNotFound() {
        givenSessionNotFound(MISSING_SESSION_ID);

        thenSaveMessageThrowsSessionNotFound(MISSING_SESSION_ID);
    }

    @Test
    @DisplayName("Mapea los mensajes de la sesión a mensajes de conversación")
    void findBySessionIdShouldReturnMappedConversationMessages() {
        givenMessagesInSession(conversationEntity(1L, MessageRole.USER, "hola"));

        List<ConversationMessage> result = findBySession();

        thenConversationMessagesAre(result, MessageRole.USER, "hola");
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando la sesión no tiene mensajes")
    void findBySessionIdShouldReturnEmptyListWhenNoMessages() {
        givenMessagesInSession();

        List<ConversationMessage> result = findBySession();

        thenConversationMessagesEmpty(result);
    }

    @Test
    @DisplayName("Devuelve la página mapeada con emoción y riesgo cuando hay emociones")
    void findByConversationIdAndUserIdShouldReturnPageWithEmotionWhenEmotionsPresent() {
        givenPageContains(
                pageEntity(1L, MessageRole.USER, "msg", true, List.of(emotion(EmotionType.SADNESS))),
                domainMessage(1L, MessageRole.USER, "msg", true, EmotionType.SADNESS));

        Page<ChatMessage> result = findByConversationAndUser();

        thenPageHasEmotionAndRisk(result, EmotionType.SADNESS);
    }

    @Test
    @DisplayName("Devuelve emoción nula cuando la lista de emociones está vacía")
    void findByConversationIdAndUserIdShouldReturnNullEmotionWhenEmotionListIsEmpty() {
        givenPageContains(
                pageEntity(2L, MessageRole.ASSISTANT, "resp", false, List.of()),
                domainMessage(2L, MessageRole.ASSISTANT, "resp", null, null));

        Page<ChatMessage> result = findByConversationAndUser();

        thenPageEmotionIsNull(result);
    }

    @Test
    @DisplayName("Devuelve emoción nula cuando la lista de emociones es nula")
    void findByConversationIdAndUserIdShouldReturnNullEmotionWhenEmotionListIsNull() {
        givenPageContains(
                pageEntity(3L, MessageRole.USER, "msg", false, null),
                domainMessage(3L, MessageRole.USER, "msg", null, null));

        Page<ChatMessage> result = findByConversationAndUser();

        thenPageEmotionIsNull(result);
    }

    @Test
    @DisplayName("Delega el conteo de mensajes de usuario desde una fecha")
    void countUserMessagesSinceShouldDelegateToJpaCountingUserRole() {
        givenUserMessagesCount(SINCE, 7L);

        long result = countUserMessagesSince(SINCE);

        thenCountIs(result, 7L);
        thenUserMessagesCountDelegated(SINCE);
    }

    @Test
    @DisplayName("Delega el conteo de mensajes de voz del usuario desde una fecha")
    void countUserAudioMessagesSinceShouldDelegateToJpaCountingVoicePrefix() {
        givenUserAudioMessagesCount(SINCE, 4L);

        long result = countUserAudioMessagesSince(SINCE);

        thenCountIs(result, 4L);
        thenUserAudioMessagesCountDelegated(SINCE);
    }

    @Test
    @DisplayName("No consulta ni guarda cuando la entrada de la acción sugerida es inválida")
    void updateSuggestedActionDecisionShouldReturnWithoutCallingJpaWhenInputIsInvalid() {
        updateSuggestedActionDecision(null, 5L, "ACCEPTED");
        updateSuggestedActionDecision(10L, null, "ACCEPTED");
        updateSuggestedActionDecision(10L, 5L, null);
        updateSuggestedActionDecision(10L, 5L, " ");

        thenSuggestedActionNeverQueriedNorSaved();
    }

    @Test
    @DisplayName("Actualiza la decisión de la acción sugerida cuando el mensaje existe")
    void updateSuggestedActionDecisionShouldUpdateDecisionWhenMessageExists() {
        ChatMessageEntity entity = messageWithSuggestedAction("PENDING");
        givenSuggestedActionMessage(entity);

        updateSuggestedActionDecision(SUGGESTED_USER_ID, EMOTIONAL_EVENT_ID, "ACCEPTED");

        thenSuggestedActionDecisionUpdated(entity, "ACCEPTED");
    }

    @Test
    @DisplayName("No guarda cuando el mensaje no tiene acción sugerida")
    void updateSuggestedActionDecisionShouldNotSaveWhenMessageHasNoSuggestedAction() {
        ChatMessageEntity entity = messageWithoutSuggestedAction();
        givenSuggestedActionMessage(entity);

        updateSuggestedActionDecision(SUGGESTED_USER_ID, EMOTIONAL_EVENT_ID, "ACCEPTED");

        thenMessageNeverSaved();
    }

    @Test
    @DisplayName("No guarda cuando no existe mensaje con la acción sugerida buscada")
    void updateSuggestedActionDecisionShouldNotSaveWhenMessageNotFound() {
        givenSuggestedActionMessage(null);

        updateSuggestedActionDecision(SUGGESTED_USER_ID, EMOTIONAL_EVENT_ID, "ACCEPTED");

        thenMessageNeverSaved();
    }

    @Test
    @DisplayName("No consulta ni guarda cuando la entrada del reto es inválida")
    void updateChallengeDecisionShouldReturnWithoutCallingJpaWhenInputIsInvalid() {
        updateChallengeDecision(null, CHALLENGE_USER_ID, TITLE, DESCRIPTION, "ACCEPTED");
        updateChallengeDecision(" ", CHALLENGE_USER_ID, TITLE, DESCRIPTION, "ACCEPTED");
        updateChallengeDecision(CONVERSATION_ID, null, TITLE, DESCRIPTION, "ACCEPTED");
        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, null, DESCRIPTION, "ACCEPTED");
        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, " ", DESCRIPTION, "ACCEPTED");
        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, TITLE, DESCRIPTION, null);
        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, TITLE, DESCRIPTION, " ");

        thenChallengeNeverQueriedNorSaved();
    }

    @Test
    @DisplayName("Normaliza la descripción nula y actualiza la decisión del reto cuando el mensaje existe")
    void updateChallengeDecisionShouldNormalizeNullDescriptionAndUpdateDecisionWhenMessageExists() {
        ChatMessageEntity entity = messageWithGeneratedChallenge("", "PENDING");
        givenChallengeMessage("", entity);

        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, TITLE, null, "ACCEPTED");

        thenChallengeDecisionUpdatedWithNormalizedDescription(entity, "ACCEPTED");
    }

    @Test
    @DisplayName("No guarda cuando el mensaje no tiene reto generado")
    void updateChallengeDecisionShouldNotSaveWhenMessageHasNoGeneratedChallenge() {
        ChatMessageEntity entity = messageWithoutGeneratedChallenge();
        givenChallengeMessage(DESCRIPTION, entity);

        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, TITLE, DESCRIPTION, "ACCEPTED");

        thenMessageNeverSaved();
    }

    @Test
    @DisplayName("No guarda cuando no existe mensaje con el reto buscado")
    void updateChallengeDecisionShouldNotSaveWhenMessageNotFound() {
        givenChallengeMessage(DESCRIPTION, null);

        updateChallengeDecision(CONVERSATION_ID, CHALLENGE_USER_ID, TITLE, DESCRIPTION, "ACCEPTED");

        thenMessageNeverSaved();
    }

    // --- arrange ---
    private void givenSession(Long sessionId) {
        when(sessionJpa.findById(sessionId))
                .thenReturn(Optional.of(ChatSessionEntity.builder().id(sessionId).conversationId(CONVERSATION_ID).build()));
    }

    private void givenSessionNotFound(Long sessionId) {
        when(sessionJpa.findById(sessionId)).thenReturn(Optional.empty());
    }

    private void givenMapperEchoesEntity() {
        when(chatMessageMapper.toEntity(any(ChatSessionEntity.class), any(ConversationMessage.class)))
                .thenAnswer(invocation -> ChatMessageEntity.builder()
                        .chatSession(invocation.getArgument(0))
                        .role(invocation.<ConversationMessage>getArgument(1).role())
                        .content(invocation.<ConversationMessage>getArgument(1).content())
                        .riskDetected(invocation.<ConversationMessage>getArgument(1).riskDetected())
                        .build());
    }

    private void givenMessagesInSession(ChatMessageEntity... entities) {
        when(jpa.findByChatSessionIdOrderByCreatedAtAsc(SESSION_ID)).thenReturn(List.of(entities));
    }

    private void givenPageContains(ChatMessageEntity entity, ChatMessage domain) {
        when(jpa.findByChatSessionConversationIdAndChatSessionAppUserId(CONVERSATION_ID, USER_ID, PAGEABLE))
                .thenReturn(new PageImpl<>(List.of(entity), PAGEABLE, 1));
        when(chatMessageMapper.toDomain(entity)).thenReturn(domain);
    }

    private void givenUserMessagesCount(Instant since, long count) {
        when(jpa.countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(USER_ID, MessageRole.USER, since)).thenReturn(count);
    }

    private void givenUserAudioMessagesCount(Instant since, long count) {
        when(jpa.countByChatSessionAppUserIdAndRoleAndContentStartingWithAndCreatedAtAfter(
                USER_ID, MessageRole.USER, AUDIO_PREFIX, since)).thenReturn(count);
    }

    private void givenSuggestedActionMessage(ChatMessageEntity entity) {
        when(jpa.findFirstByChatSessionAppUserIdAndRoleAndSuggestedActionEmotionalEventIdOrderByCreatedAtDesc(
                SUGGESTED_USER_ID, MessageRole.ASSISTANT, EMOTIONAL_EVENT_ID))
                .thenReturn(Optional.ofNullable(entity));
    }

    private void givenChallengeMessage(String description, ChatMessageEntity entity) {
        when(jpa.findFirstByChatSessionConversationIdAndChatSessionAppUserIdAndRoleAndGeneratedChallengeTitleAndGeneratedChallengeDescriptionOrderByCreatedAtDesc(
                CONVERSATION_ID, CHALLENGE_USER_ID, MessageRole.ASSISTANT, TITLE, description))
                .thenReturn(Optional.ofNullable(entity));
    }

    private ConversationMessage conversationMessage(MessageRole role, String content, EmotionType emotion) {
        return new ConversationMessage(role, content, emotion, false, null, null, null, null, null);
    }

    private ChatMessageEntity conversationEntity(Long id, MessageRole role, String content) {
        return ChatMessageEntity.builder().id(id).role(role).content(content).build();
    }

    private ChatMessageEntity pageEntity(Long id, MessageRole role, String content, boolean risk, List<EmotionEntity> emotions) {
        return ChatMessageEntity.builder()
                .id(id).role(role).content(content).riskDetected(risk).createdAt(CREATED_AT).emotions(emotions).build();
    }

    private ChatMessage domainMessage(Long id, MessageRole role, String content, Boolean risk, EmotionType emotion) {
        return new ChatMessage(id, role, content, risk, emotion, CREATED_AT, null, null, null, null);
    }

    private EmotionEntity emotion(EmotionType type) {
        return EmotionEntity.builder().emotionDetected(type).build();
    }

    private ChatMessageEntity messageWithSuggestedAction(String decision) {
        return ChatMessageEntity.builder()
                .id(1L)
                .suggestedAction(SuggestedActionEmbeddable.builder()
                        .type("BREATHING")
                        .emotionalEventId(EMOTIONAL_EVENT_ID)
                        .decision(decision)
                        .build())
                .build();
    }

    private ChatMessageEntity messageWithoutSuggestedAction() {
        return ChatMessageEntity.builder().id(1L).suggestedAction(null).build();
    }

    private ChatMessageEntity messageWithGeneratedChallenge(String description, String decision) {
        return ChatMessageEntity.builder()
                .id(2L)
                .generatedChallenge(GeneratedChallengeEmbeddable.builder()
                        .title(TITLE)
                        .description(description)
                        .decision(decision)
                        .build())
                .build();
    }

    private ChatMessageEntity messageWithoutGeneratedChallenge() {
        return ChatMessageEntity.builder().id(2L).generatedChallenge(null).build();
    }

    // --- act ---
    private void saveMessage(Long sessionId, ConversationMessage message) {
        repository.saveMessage(sessionId, message);
    }

    private List<ConversationMessage> findBySession() {
        return repository.findBySessionId(SESSION_ID);
    }

    private Page<ChatMessage> findByConversationAndUser() {
        return repository.findByConversationIdAndUserId(CONVERSATION_ID, USER_ID, PAGEABLE);
    }

    private long countUserMessagesSince(Instant since) {
        return repository.countUserMessagesSince(USER_ID, since);
    }

    private long countUserAudioMessagesSince(Instant since) {
        return repository.countUserAudioMessagesSince(USER_ID, since);
    }

    private void updateSuggestedActionDecision(Long userId, Long emotionalEventId, String decision) {
        repository.updateSuggestedActionDecision(userId, emotionalEventId, decision);
    }

    private void updateChallengeDecision(String conversationId, Long userId, String title, String description, String decision) {
        repository.updateChallengeDecision(conversationId, userId, title, description, decision);
    }

    // --- assert ---
    private void thenPersistedEntityHasNoEmotion(String content, MessageRole role) {
        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getEmotions()).isNull();
        assertThat(captor.getValue().getContent()).isEqualTo(content);
        assertThat(captor.getValue().getRole()).isEqualTo(role);
    }

    private void thenPersistedEntityHasEmotion(EmotionType emotion) {
        ArgumentCaptor<ChatMessageEntity> captor = ArgumentCaptor.forClass(ChatMessageEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getEmotions()).hasSize(1);
        assertThat(captor.getValue().getEmotions().get(0).getEmotionDetected()).isEqualTo(emotion);
    }

    private void thenSaveMessageThrowsSessionNotFound(Long sessionId) {
        assertThatThrownBy(() -> repository.saveMessage(sessionId, ConversationMessage.of(MessageRole.USER, "hola")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.valueOf(sessionId));
    }

    private void thenConversationMessagesAre(List<ConversationMessage> result, MessageRole role, String content) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo(role);
        assertThat(result.get(0).content()).isEqualTo(content);
    }

    private void thenConversationMessagesEmpty(List<ConversationMessage> result) {
        assertThat(result).isEmpty();
    }

    private void thenPageHasEmotionAndRisk(Page<ChatMessage> result, EmotionType emotion) {
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).detectedEmotion()).isEqualTo(emotion);
        assertThat(result.getContent().get(0).riskDetected()).isTrue();
    }

    private void thenPageEmotionIsNull(Page<ChatMessage> result) {
        assertThat(result.getContent().get(0).detectedEmotion()).isNull();
    }

    private void thenCountIs(long result, long expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenUserMessagesCountDelegated(Instant since) {
        verify(jpa).countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(USER_ID, MessageRole.USER, since);
    }

    private void thenUserAudioMessagesCountDelegated(Instant since) {
        verify(jpa).countByChatSessionAppUserIdAndRoleAndContentStartingWithAndCreatedAtAfter(
                USER_ID, MessageRole.USER, AUDIO_PREFIX, since);
    }

    private void thenSuggestedActionNeverQueriedNorSaved() {
        verify(jpa, never()).findFirstByChatSessionAppUserIdAndRoleAndSuggestedActionEmotionalEventIdOrderByCreatedAtDesc(
                any(), any(), any());
        verify(jpa, never()).save(any(ChatMessageEntity.class));
    }

    private void thenSuggestedActionDecisionUpdated(ChatMessageEntity entity, String decision) {
        assertThat(entity.getSuggestedAction().getDecision()).isEqualTo(decision);
        verify(jpa).save(entity);
    }

    private void thenChallengeNeverQueriedNorSaved() {
        verify(jpa, never()).findFirstByChatSessionConversationIdAndChatSessionAppUserIdAndRoleAndGeneratedChallengeTitleAndGeneratedChallengeDescriptionOrderByCreatedAtDesc(
                any(), any(), any(), any(), any());
        verify(jpa, never()).save(any(ChatMessageEntity.class));
    }

    private void thenChallengeDecisionUpdatedWithNormalizedDescription(ChatMessageEntity entity, String decision) {
        assertThat(entity.getGeneratedChallenge().getDecision()).isEqualTo(decision);
        verify(jpa).findFirstByChatSessionConversationIdAndChatSessionAppUserIdAndRoleAndGeneratedChallengeTitleAndGeneratedChallengeDescriptionOrderByCreatedAtDesc(
                eq(CONVERSATION_ID), eq(CHALLENGE_USER_ID), eq(MessageRole.ASSISTANT), eq(TITLE), eq(""));
        verify(jpa).save(entity);
    }

    private void thenMessageNeverSaved() {
        verify(jpa, never()).save(any(ChatMessageEntity.class));
    }
}
