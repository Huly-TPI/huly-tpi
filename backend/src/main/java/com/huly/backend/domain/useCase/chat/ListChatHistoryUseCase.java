package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ListChatHistoryUseCase {

    private final ChatMessageRepository chatMessageRepository;
    private final InitializeChatPreferencesUseCase initializeChatPreferencesUseCase;

    public Page<ChatMessage> execute(String conversationId, Long userId, Pageable pageable) {
        initializeChatPreferencesUseCase.execute(userId, conversationId);
        return chatMessageRepository.findByConversationIdAndUserId(conversationId, userId, pageable);
    }
}
