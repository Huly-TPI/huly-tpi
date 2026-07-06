package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatHistoryRequest;
import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceInitializationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListChatHistoryUseCaseTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatPreferenceInitializationService chatPreferenceInitializationService;
    @Spy
    private ChatMapper mapper = new ChatMapper();

    @InjectMocks
    private ListChatHistoryUseCase listChatHistoryUseCase;

    @Test
    void execute_shouldDelegateToRepositoryAndReturnPage() {
        String conversationId = "conv-1";
        Long userId = 1L;
        ChatMessage msg = new ChatMessage(1L, MessageRole.USER, "hola", false, EmotionType.NEUTRAL, Instant.now(), null, null, null, null);
        Page<ChatMessage> expected = new PageImpl<>(List.of(msg), PageRequest.of(0, 10), 1);

        when(chatMessageRepository.findByConversationIdAndUserId(eq(conversationId), eq(userId), any(Pageable.class)))
                .thenReturn(expected);

        ChatHistoryResponse result = listChatHistoryUseCase.execute(new ChatHistoryRequest(userId, conversationId, 0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).content()).isEqualTo("hola");
        assertThat(result.totalElements()).isEqualTo(1);
        verify(chatPreferenceInitializationService).initialize(userId, conversationId);
        verify(chatMessageRepository).findByConversationIdAndUserId(eq(conversationId), eq(userId), any(Pageable.class));
    }

    @Test
    void execute_shouldReturnEmptyPageWhenNoMessages() {
        String conversationId = "conv-sin-mensajes";
        Long userId = 1L;
        Page<ChatMessage> emptyPage = Page.empty(PageRequest.of(0, 10));

        when(chatMessageRepository.findByConversationIdAndUserId(eq(conversationId), eq(userId), any(Pageable.class)))
                .thenReturn(emptyPage);

        ChatHistoryResponse result = listChatHistoryUseCase.execute(new ChatHistoryRequest(userId, conversationId, 0, 10));

        assertThat(result.content()).isEmpty();
        verify(chatPreferenceInitializationService).initialize(userId, conversationId);
        verify(chatMessageRepository).findByConversationIdAndUserId(eq(conversationId), eq(userId), any(Pageable.class));
    }

    @Test
    void execute_shouldNormalizeInvalidPagination() {
        when(chatMessageRepository.findByConversationIdAndUserId(eq("conv-1"), eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 1)));

        ChatHistoryResponse result = listChatHistoryUseCase.execute(new ChatHistoryRequest(1L, "conv-1", -1, 0));

        assertThat(result.content()).isEmpty();
        verify(chatMessageRepository).findByConversationIdAndUserId(eq("conv-1"), eq(1L),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 1));
    }

    @Test
    void execute_shouldPropagateExceptionFromRepository() {
        when(chatMessageRepository.findByConversationIdAndUserId(eq("conv-err"), eq(1L), any(Pageable.class)))
                .thenThrow(new RuntimeException("error de repositorio"));

        assertThatThrownBy(() -> listChatHistoryUseCase.execute(new ChatHistoryRequest(1L, "conv-err", 0, 10)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error de repositorio");
    }
}
