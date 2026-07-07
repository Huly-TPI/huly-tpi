package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.dto.chat.ChatHistoryRequest;
import com.huly.backend.domain.dto.chat.ChatHistoryResponse;
import com.huly.backend.domain.mapper.chat.ChatMapper;
import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.service.chat.ChatPreferenceInitializationService;
import org.junit.jupiter.api.DisplayName;
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

    private static final Long USER_ID = 1L;
    private static final String CONVERSATION_ID = "conv-1";

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private ChatPreferenceInitializationService chatPreferenceInitializationService;
    @Spy
    private ChatMapper mapper = new ChatMapper();

    @InjectMocks
    private ListChatHistoryUseCase listChatHistoryUseCase;

    private ChatHistoryRequest request;

    @Test
    @DisplayName("Delega en el repositorio y devuelve la página mapeada")
    void executeShouldDelegateToRepositoryAndReturnPage() {
        // --- arrange ---
        givenRequest(USER_ID, CONVERSATION_ID, 0, 10);
        givenRepositoryReturnsOneMessage("hola");
        // --- act ---
        ChatHistoryResponse result = execute();
        // --- assert ---
        thenResponseContainsSingleMessage(result, "hola", 1);
        thenPreferenceServiceInitialized();
        thenRepositoryQueried();
    }

    @Test
    @DisplayName("Devuelve una página vacía cuando no hay mensajes")
    void executeShouldReturnEmptyPageWhenNoMessages() {
        // --- arrange ---
        givenRequest(USER_ID, "conv-sin-mensajes", 0, 10);
        givenRepositoryReturnsEmptyPage(10);
        // --- act ---
        ChatHistoryResponse result = execute();
        // --- assert ---
        thenResponseIsEmpty(result);
        thenPreferenceServiceInitialized();
        thenRepositoryQueried();
    }

    @Test
    @DisplayName("Normaliza la paginación inválida (page<0 y size<1)")
    void executeShouldNormalizeInvalidPagination() {
        // --- arrange ---
        givenRequest(USER_ID, CONVERSATION_ID, -1, 0);
        givenRepositoryReturnsEmptyPage(1);
        // --- act ---
        ChatHistoryResponse result = execute();
        // --- assert ---
        thenResponseIsEmpty(result);
        thenRepositoryQueriedWithNormalizedPageable();
    }

    @Test
    @DisplayName("Propaga la excepción lanzada por el repositorio")
    void executeShouldPropagateExceptionFromRepository() {
        // --- arrange ---
        givenRequest(USER_ID, "conv-err", 0, 10);
        givenRepositoryThrows("error de repositorio");
        // --- act + assert ---
        thenExecuteThrowsRuntimeWithMessage("error de repositorio");
    }

    // --- arrange ---

    private void givenRequest(Long userId, String conversationId, int page, int size) {
        request = new ChatHistoryRequest(userId, conversationId, page, size);
    }

    private void givenRepositoryReturnsOneMessage(String content) {
        ChatMessage message = new ChatMessage(
                1L, MessageRole.USER, content, false, EmotionType.NEUTRAL, Instant.now(), null, null, null, null);
        Page<ChatMessage> page = new PageImpl<>(List.of(message), PageRequest.of(0, 10), 1);
        when(chatMessageRepository.findByConversationIdAndUserId(
                eq(request.conversationId()), eq(request.userId()), any(Pageable.class)))
                .thenReturn(page);
    }

    private void givenRepositoryReturnsEmptyPage(int size) {
        when(chatMessageRepository.findByConversationIdAndUserId(
                eq(request.conversationId()), eq(request.userId()), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, size)));
    }

    private void givenRepositoryThrows(String message) {
        when(chatMessageRepository.findByConversationIdAndUserId(
                eq(request.conversationId()), eq(request.userId()), any(Pageable.class)))
                .thenThrow(new RuntimeException(message));
    }

    // --- act ---

    private ChatHistoryResponse execute() {
        return listChatHistoryUseCase.execute(request);
    }

    // --- assert ---

    private void thenResponseContainsSingleMessage(ChatHistoryResponse result, String content, long totalElements) {
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).content()).isEqualTo(content);
        assertThat(result.totalElements()).isEqualTo(totalElements);
    }

    private void thenResponseIsEmpty(ChatHistoryResponse result) {
        assertThat(result.content()).isEmpty();
    }

    private void thenPreferenceServiceInitialized() {
        verify(chatPreferenceInitializationService).initialize(request.userId(), request.conversationId());
    }

    private void thenRepositoryQueried() {
        verify(chatMessageRepository).findByConversationIdAndUserId(
                eq(request.conversationId()), eq(request.userId()), any(Pageable.class));
    }

    private void thenRepositoryQueriedWithNormalizedPageable() {
        verify(chatMessageRepository).findByConversationIdAndUserId(
                eq(request.conversationId()), eq(request.userId()),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 1));
    }

    private void thenExecuteThrowsRuntimeWithMessage(String message) {
        assertThatThrownBy(this::execute)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(message);
    }
}
