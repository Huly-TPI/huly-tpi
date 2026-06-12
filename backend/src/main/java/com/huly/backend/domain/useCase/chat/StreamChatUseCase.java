package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class StreamChatUseCase {

    private final ChatService chatService;

    public Flux<ChatStreamEvent> execute(String message, String conversationId, Long userId) {
        return chatService.streamMessage(message, conversationId, userId);
    }
}
