package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
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

    // ── findConversationIdBySessionId ────────────────────────────────────────

    @Test
    void findConversationIdBySessionId_shouldReturnConversationId_whenSessionExists() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(1L).conversationId("conv-abc").build();
        when(jpa.findById(1L)).thenReturn(Optional.of(session));

        Optional<String> result = repository.findConversationIdBySessionId(1L);

        assertThat(result).contains("conv-abc");
    }

    @Test
    void findConversationIdBySessionId_shouldReturnEmpty_whenSessionNotExists() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());

        Optional<String> result = repository.findConversationIdBySessionId(99L);

        assertThat(result).isEmpty();
    }

    // ── saveSession ──────────────────────────────────────────────────────────

    @Test
    void saveSession_shouldSaveWithUser_whenUserIdExists() {
        AppUserEntity user = new AppUserEntity();
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(user));
        ChatSessionEntity saved = ChatSessionEntity.builder().id(5L).conversationId("conv-1").appUser(user).build();
        when(jpa.save(any())).thenReturn(saved);

        Long result = repository.saveSession("conv-1", 10L);

        assertThat(result).isEqualTo(5L);
        ArgumentCaptor<ChatSessionEntity> captor = ArgumentCaptor.forClass(ChatSessionEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getConversationId()).isEqualTo("conv-1");
        assertThat(captor.getValue().getAppUser()).isEqualTo(user);
    }

    @Test
    void saveSession_shouldSaveWithNullUser_whenUserNotFound() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());
        ChatSessionEntity saved = ChatSessionEntity.builder().id(6L).conversationId("conv-2").build();
        when(jpa.save(any())).thenReturn(saved);

        Long result = repository.saveSession("conv-2", 99L);

        assertThat(result).isEqualTo(6L);
        ArgumentCaptor<ChatSessionEntity> captor = ArgumentCaptor.forClass(ChatSessionEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getAppUser()).isNull();
    }

    @Test
    void saveSession_shouldSaveWithNullUser_whenUserIdIsNull() {
        ChatSessionEntity saved = ChatSessionEntity.builder().id(7L).conversationId("conv-3").build();
        when(jpa.save(any())).thenReturn(saved);

        Long result = repository.saveSession("conv-3", null);

        assertThat(result).isEqualTo(7L);
        verify(appUserRepository, never()).findById(any());
        ArgumentCaptor<ChatSessionEntity> captor = ArgumentCaptor.forClass(ChatSessionEntity.class);
        verify(jpa).save(captor.capture());
        assertThat(captor.getValue().getAppUser()).isNull();
    }

    // ── findSessionIdByConversationIdAndUserId ───────────────────────────────

    @Test
    void findSessionIdByConversationIdAndUserId_shouldReturnSessionId_whenSessionExists() {
        ChatSessionEntity session = ChatSessionEntity.builder().id(3L).conversationId("conv-1").build();
        when(jpa.findByConversationIdAndAppUserId("conv-1", 10L)).thenReturn(Optional.of(session));

        Optional<Long> result = repository.findSessionIdByConversationIdAndUserId("conv-1", 10L);

        assertThat(result).contains(3L);
    }

    @Test
    void findSessionIdByConversationIdAndUserId_shouldReturnEmpty_whenSessionNotExists() {
        when(jpa.findByConversationIdAndAppUserId("conv-x", 10L)).thenReturn(Optional.empty());

        Optional<Long> result = repository.findSessionIdByConversationIdAndUserId("conv-x", 10L);

        assertThat(result).isEmpty();
    }

    @Test
    void findSessionIdByConversationIdAndUserId_shouldReturnEmpty_whenUserIdIsNull() {
        Optional<Long> result = repository.findSessionIdByConversationIdAndUserId("conv-x", null);

        assertThat(result).isEmpty();
        verify(jpa, never()).findByConversationIdAndAppUserId(any(), any());
    }
}
