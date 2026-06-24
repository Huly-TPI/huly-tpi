package com.huly.backend.domain.repository.chat;

import com.huly.backend.domain.model.chat.ChatMessage;
import com.huly.backend.domain.model.chat.ConversationMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository {

    void saveMessage(Long sessionId, ConversationMessage message);

    List<ConversationMessage> findBySessionId(Long sessionId);

    Page<ChatMessage> findByConversationIdAndUserId(String conversationId, Long userId, Pageable pageable);

    void updateSuggestedActionDecision(Long userId, Long emotionalEventId, String decision);

    void updateChallengeDecision(String conversationId, Long userId, String title, String description, String decision);

    long countUserMessagesSince(Long userId, Instant since);

    long countUserAudioMessagesSince(Long userId, Instant since);
}
