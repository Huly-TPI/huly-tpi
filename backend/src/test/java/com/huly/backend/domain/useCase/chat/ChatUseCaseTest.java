package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.enums.EmotionType;
import com.huly.backend.domain.service.chat.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatUseCaseTest {

    @Mock
    private ChatService chatService;
    @Mock
    private HandleChatPreferencesUseCase handleChatPreferencesUseCase;

    @InjectMocks
    private ChatUseCase chatUseCase;

    @Test
    void execute_shouldDelegateToServiceAndReturnReply() {
        String message = "me siento mal";
        String conversationId = "conv-1";
        Long userId = 42L;
        ChatReply expected = new ChatReply("respuesta", EmotionType.SADNESS, 7, false, null);

        when(handleChatPreferencesUseCase.execute(userId, conversationId, message))
                .thenReturn(Optional.empty());
        when(chatService.processMessage(message, conversationId, userId)).thenReturn(expected);

        ChatReply result = chatUseCase.execute(message, conversationId, userId);

        assertThat(result).isEqualTo(expected);
        verify(chatService).processMessage(message, conversationId, userId);
    }

    @Test
    void execute_shouldReturnPreferenceReplyWithoutCallingChatService_whenPreferenceWasHandled() {
        ChatReply expected = ChatReply.of("Listo, te voy a decir Checho.");
        when(handleChatPreferencesUseCase.execute(42L, "conv-1", "decime Checho"))
                .thenReturn(Optional.of(expected));

        ChatReply result = chatUseCase.execute("decime Checho", "conv-1", 42L);

        assertThat(result).isEqualTo(expected);
        verify(handleChatPreferencesUseCase).execute(42L, "conv-1", "decime Checho");
        verifyNoInteractions(chatService);
    }

    @Test
    void execute_shouldPropagateExceptionFromService() {
        when(handleChatPreferencesUseCase.execute(1L, "conv-2", "msg"))
                .thenReturn(Optional.empty());
        when(chatService.processMessage("msg", "conv-2", 1L))
                .thenThrow(new RuntimeException("error de servicio"));

        assertThatThrownBy(() -> chatUseCase.execute("msg", "conv-2", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("error de servicio");
    }
}
