package com.huly.backend.infrastructure.adapter.memory;

import com.huly.backend.domain.model.chat.ConversationMessage;
import com.huly.backend.domain.provider.ChatMemoryPort;
import com.huly.backend.domain.repository.chat.ChatMessageRepository;
import com.huly.backend.domain.repository.chat.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaChatMemoryAdapter implements ChatMemoryPort {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public List<ConversationMessage> getHistory(String conversationId) {
        return chatSessionRepository.findSessionIdByConversationId(conversationId)
                .map(chatMessageRepository::findBySessionId)
                .orElse(List.of());
    }

    @Override
    public void addMessage(String conversationId, ConversationMessage message) {
        Long sessionId = chatSessionRepository.findSessionIdByConversationId(conversationId)
                .orElseGet(() -> chatSessionRepository.saveSession(conversationId));

        chatMessageRepository.saveMessage(sessionId, message);
    }
}