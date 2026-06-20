package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListChatHistoryUseCaseTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;

    @InjectMocks
    private ListChatHistoryUseCase listChatHistoryUseCase;

    @Test
    void execute_shouldDelegateToRepositoryAndReturnPage() {
        String conversationId = "conv-1";
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        ChatMessage msg = new ChatMessage(1L, MessageRole.USER, "hola", false, EmotionType.NEUTRAL, Instant.now(), null, null, null, null);
        Page<ChatMessage> expected = new PageImpl<>(List.of(msg), pageable, 1);

        when(chatMessageRepository.findByConversationIdAndUserId(conversationId, userId, pageable)).thenReturn(expected);

        Page<ChatMessage> result = listChatHistoryUseCase.execute(conversationId, userId, pageable);

        assertThat(result).isEqualTo(expected);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("hola");
        verify(initializeChatPreferencesUseCase).execute(userId, conversationId);
        verify(chatMessageRepository).findByConversationIdAndUserId(conversationId, userId, pageable);
    }

    @Test
    void execute_shouldReturnEmptyPageWhenNoMessages() {
        String conversationId = "conv-sin-mensajes";
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChatMessage> emptyPage = Page.empty(pageable);

        when(chatMessageRepository.findByConversationIdAndUserId(conversationId, userId, pageable)).thenReturn(emptyPage);

        Page<ChatMessage> result = listChatHistoryUseCase.execute(conversationId, userId, pageable);

        assertThat(result).isEmpty();
        verify(initializeChatPreferencesUseCase).execute(userId, conversationId);
        verify(chatMessageRepository).findByConversationIdAndUserId(conversationId, userId, pageable);
    }

    @Test
    void execute_shouldPropagateExceptionFromRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        when(chatMessageRepository.findByConversationIdAndUserId("conv-err", 1L, pageable))
                .thenThrow(new RuntimeException("error de repositorio"));

        assertThatThrownBy(() -> listChatHistoryUseCase.execute("conv-err", 1L, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error de repositorio");
    }
}
