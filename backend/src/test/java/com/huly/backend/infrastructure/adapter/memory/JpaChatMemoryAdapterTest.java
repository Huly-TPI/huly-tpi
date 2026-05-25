package com.huly.backend.infrastructure.adapter.memory;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chat.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaChatMemoryAdapterTest {

    @Mock private ChatSessionRepository chatSessionRepository;
    @Mock private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private JpaChatMemoryAdapter adapter;

    // ── getHistory ───────────────────────────────────────────────────────────

    @Test
    void getHistory_shouldReturnMessages_whenSessionExists() {
        List<ConversationMessage> messages = List.of(ConversationMessage.of(MessageRole.USER, "hola"));
        when(chatSessionRepository.findSessionIdByConversationId("conv-1")).thenReturn(Optional.of(10L));
        when(chatMessageRepository.findBySessionId(10L)).thenReturn(messages);

        List<ConversationMessage> result = adapter.getHistory("conv-1");

        assertThat(result).isEqualTo(messages);
        verify(chatMessageRepository).findBySessionId(10L);
    }

    @Test
    void getHistory_shouldReturnEmptyList_whenSessionNotFound() {
        when(chatSessionRepository.findSessionIdByConversationId("conv-x")).thenReturn(Optional.empty());

        List<ConversationMessage> result = adapter.getHistory("conv-x");

        assertThat(result).isEmpty();
        verify(chatMessageRepository, never()).findBySessionId(anyLong());
    }

    // ── addMessage ───────────────────────────────────────────────────────────

    @Test
    void addMessage_shouldUseExistingSession_whenSessionAlreadyExists() {
        when(chatSessionRepository.findSessionIdByConversationId("conv-1")).thenReturn(Optional.of(5L));
        ConversationMessage msg = ConversationMessage.of(MessageRole.USER, "hola");

        adapter.addMessage("conv-1", msg, 1L);

        verify(chatMessageRepository).saveMessage(5L, msg);
        verify(chatSessionRepository, never()).saveSession(anyString(), any());
    }

    @Test
    void addMessage_shouldCreateNewSession_whenSessionNotExists() {
        when(chatSessionRepository.findSessionIdByConversationId("conv-nueva")).thenReturn(Optional.empty());
        when(chatSessionRepository.saveSession("conv-nueva", 1L)).thenReturn(7L);
        ConversationMessage msg = ConversationMessage.of(MessageRole.ASSISTANT, "resp");

        adapter.addMessage("conv-nueva", msg, 1L);

        verify(chatSessionRepository).saveSession("conv-nueva", 1L);
        verify(chatMessageRepository).saveMessage(7L, msg);
    }
}
