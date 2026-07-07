package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatSessionRepositoryImplTest {

    @Mock private IChatSessionJpaRepository jpa;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks
    private ChatSessionRepositoryImpl repository;

    @Test
    @DisplayName("Devuelve el conversationId cuando la sesión existe")
    void findConversationIdBySessionIdShouldReturnConversationIdWhenSessionExists() {
        givenSessionById(1L, session(1L, "conv-abc"));

        Optional<String> result = findConversationId(1L);

        thenConversationIdContains(result, "conv-abc");
    }

    @Test
    @DisplayName("Devuelve vacío cuando la sesión no existe al buscar conversationId")
    void findConversationIdBySessionIdShouldReturnEmptyWhenSessionNotExists() {
        givenNoSessionById(99L);

        Optional<String> result = findConversationId(99L);

        thenConversationIdEmpty(result);
    }

    @Test
    @DisplayName("Guarda la sesión con usuario cuando el usuario existe")
    void saveSessionShouldSaveWithUserWhenUserIdExists() {
        AppUserEntity user = appUser();
        givenUserFound(10L, user);
        givenSaved(session(5L, "conv-1", user));

        Long result = saveSession("conv-1", 10L);

        thenSessionIdIs(result, 5L);
        thenSavedSessionHasUser("conv-1", user);
    }

    @Test
    @DisplayName("Guarda la sesión con usuario nulo cuando el usuario no se encuentra")
    void saveSessionShouldSaveWithNullUserWhenUserNotFound() {
        givenUserNotFound(99L);
        givenSaved(session(6L, "conv-2"));

        Long result = saveSession("conv-2", 99L);

        thenSessionIdIs(result, 6L);
        thenSavedSessionHasNullUser();
    }

    @Test
    @DisplayName("Guarda la sesión con usuario nulo cuando el userId es nulo")
    void saveSessionShouldSaveWithNullUserWhenUserIdIsNull() {
        givenSaved(session(7L, "conv-3"));

        Long result = saveSession("conv-3", null);

        thenSessionIdIs(result, 7L);
        thenUserLookupSkipped();
        thenSavedSessionHasNullUser();
    }

    @Test
    @DisplayName("Devuelve el sessionId cuando la sesión existe")
    void findSessionIdByConversationIdAndUserIdShouldReturnSessionIdWhenSessionExists() {
        givenSessionByConversationAndUser("conv-1", 10L, session(3L, "conv-1"));

        Optional<Long> result = findSessionId("conv-1", 10L);

        thenSessionIdFound(result, 3L);
    }

    @Test
    @DisplayName("Devuelve vacío cuando la sesión no existe al buscar sessionId")
    void findSessionIdByConversationIdAndUserIdShouldReturnEmptyWhenSessionNotExists() {
        givenNoSessionByConversationAndUser("conv-x", 10L);

        Optional<Long> result = findSessionId("conv-x", 10L);

        thenSessionIdEmpty(result);
    }

    @Test
    @DisplayName("Devuelve vacío cuando el userId es nulo al buscar sessionId")
    void findSessionIdByConversationIdAndUserIdShouldReturnEmptyWhenUserIdIsNull() {
        Optional<Long> result = findSessionId("conv-x", null);

        thenSessionIdEmpty(result);
        thenConversationLookupSkipped();
    }

    // --- arrange ---
    private void givenSessionById(Long id, ChatSessionEntity session) {
        when(jpa.findById(id)).thenReturn(Optional.of(session));
    }

    private void givenNoSessionById(Long id) {
        when(jpa.findById(id)).thenReturn(Optional.empty());
    }

    private void givenUserFound(Long id, AppUserEntity user) {
        when(appUserRepository.findById(id)).thenReturn(Optional.of(user));
    }

    private void givenUserNotFound(Long id) {
        when(appUserRepository.findById(id)).thenReturn(Optional.empty());
    }

    private void givenSaved(ChatSessionEntity saved) {
        when(jpa.save(any())).thenReturn(saved);
    }

    private void givenSessionByConversationAndUser(String conversationId, Long userId, ChatSessionEntity session) {
        when(jpa.findByConversationIdAndAppUserId(conversationId, userId)).thenReturn(Optional.of(session));
    }

    private void givenNoSessionByConversationAndUser(String conversationId, Long userId) {
        when(jpa.findByConversationIdAndAppUserId(conversationId, userId)).thenReturn(Optional.empty());
    }

    private ChatSessionEntity session(Long id, String conversationId) {
        return ChatSessionEntity.builder().id(id).conversationId(conversationId).build();
    }

    private ChatSessionEntity session(Long id, String conversationId, AppUserEntity user) {
        return ChatSessionEntity.builder().id(id).conversationId(conversationId).appUser(user).build();
    }

    private AppUserEntity appUser() {
        return new AppUserEntity();
    }

    // --- act ---
    private Optional<String> findConversationId(Long sessionId) {
        return repository.findConversationIdBySessionId(sessionId);
    }

    private Long saveSession(String conversationId, Long userId) {
        return repository.saveSession(conversationId, userId);
    }

    private Optional<Long> findSessionId(String conversationId, Long userId) {
        return repository.findSessionIdByConversationIdAndUserId(conversationId, userId);
    }

    // --- assert ---
    private void thenConversationIdContains(Optional<String> result, String expected) {
        assertThat(result).contains(expected);
    }

    private void thenConversationIdEmpty(Optional<String> result) {
        assertThat(result).isEmpty();
    }

    private void thenSessionIdIs(Long result, Long expected) {
        assertThat(result).isEqualTo(expected);
    }

    private void thenSavedSessionHasUser(String conversationId, AppUserEntity user) {
        ArgumentCaptor<ChatSessionEntity> captor = ArgumentCaptor.forClass(ChatSessionEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getConversationId()).isEqualTo(conversationId);
        assertThat(captor.getValue().getAppUser()).isEqualTo(user);
    }

    private void thenSavedSessionHasNullUser() {
        ArgumentCaptor<ChatSessionEntity> captor = ArgumentCaptor.forClass(ChatSessionEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getAppUser()).isNull();
    }

    private void thenUserLookupSkipped() {
        verify(appUserRepository, never()).findById(any());
    }

    private void thenSessionIdFound(Optional<Long> result, Long expected) {
        assertThat(result).contains(expected);
    }

    private void thenSessionIdEmpty(Optional<Long> result) {
        assertThat(result).isEmpty();
    }

    private void thenConversationLookupSkipped() {
        verify(jpa, never()).findByConversationIdAndAppUserId(any(), any());
    }
}
