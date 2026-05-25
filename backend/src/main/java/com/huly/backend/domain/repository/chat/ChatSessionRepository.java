package com.huly.backend.domain.repository.chat;

import java.util.Optional;

public interface ChatSessionRepository {

    Optional<String> findConversationIdBySessionId(Long sessionId);

    Long saveSession(String conversationId);

    Optional<Long> findSessionIdByConversationId(String conversationId);
}