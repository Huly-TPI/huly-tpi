package com.huly.backend.domain.repository.chat;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ConversationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatMessageRepository {

    void saveMessage(Long sessionId, ConversationMessage message);

    List<ConversationMessage> findBySessionId(Long sessionId);

    Page<ChatMessage> findByConversationId(String conversationId, Pageable pageable);
}