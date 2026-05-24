package com.huly.backend.domain.provider;

import com.huly.backend.domain.model.ConversationMessage;

import java.util.List;

public interface ChatMemoryPort {

    List<ConversationMessage> getHistory(String conversationId);

    void addMessage(String conversationId, ConversationMessage message);
}