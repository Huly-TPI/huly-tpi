package com.huly.backend.domain.useCase.chat;

import com.huly.backend.domain.model.chat.ChatStreamEvent;
import com.huly.backend.domain.model.chat.ChatPreferenceHandlingResult;
import com.huly.backend.domain.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class StreamChatUseCase {

    private final ChatService chatService;
    private final HandleChatPreferencesUseCase handleChatPreferencesUseCase;

    public Flux<ChatStreamEvent> execute(String message, String conversationId, Long userId) {
        ChatPreferenceHandlingResult preferenceResult =
                handleChatPreferencesUseCase.execute(userId, conversationId, message);
        if (!preferenceResult.continueConversation()) {
            return Flux.just(
                    ChatStreamEvent.delta(preferenceResult.directReply().content()),
                    ChatStreamEvent.metadata(preferenceResult.directReply()),
                    ChatStreamEvent.done(preferenceResult.directReply()));
        }
        return chatService.streamMessage(
                message,
                conversationId,
                userId,
                preferenceResult.offerCommunicationStyleWhenSafe());
    }
}
