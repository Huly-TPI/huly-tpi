package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatUseCase {

    private final ChatService chatService;

    public ChatReply execute(String message, String conversationId, Long userId) {
        return chatService.processMessage(message, conversationId, userId);
    }
}

