package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatReply;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.service.chat.ChatService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatUseCase {

    private final ChatService chatService;
    private final HandleChatPreferencesUseCase handleChatPreferencesUseCase;

    public ChatReply execute(String message, String conversationId, Long userId) {
        ChatPreferenceHandlingResult preferenceResult =
                handleChatPreferencesUseCase.execute(userId, conversationId, message);
        if (!preferenceResult.continueConversation()) {
            return preferenceResult.directReply();
        }
        return chatService.processMessage(
                message,
                conversationId,
                userId,
                preferenceResult.offerCommunicationStyleWhenSafe());
    }
}
