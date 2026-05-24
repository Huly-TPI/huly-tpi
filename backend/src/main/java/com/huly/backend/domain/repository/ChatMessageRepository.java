package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.ConversationMessage;

import java.util.List;

public interface ChatMessageRepository {

    void saveMessage(Long sessionId, ConversationMessage message);

    List<ConversationMessage> findBySessionId(Long sessionId);
}