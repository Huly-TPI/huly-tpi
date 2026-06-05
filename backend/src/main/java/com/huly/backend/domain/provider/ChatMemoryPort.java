package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.chat.ConversationMessage;

import java.util.List;

public interface ChatMemoryPort {

    List<ConversationMessage> getHistory(String conversationId, Long userId);

    void addMessage(String conversationId, ConversationMessage message, Long userId);
}