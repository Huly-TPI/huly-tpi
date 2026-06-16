package com.huly.backend.domain.repository.chat;

import com.huly.backend.domain.model.chat.ChatConversationPreference;

import java.util.Optional;

/**
 * Persistence port for user conversational preferences.
 */
public interface ChatConversationPreferenceRepository {

    /**
     * Finds preferences for one user.
     *
     * @param userId user identifier
     * @return stored preferences when present
     */
    Optional<ChatConversationPreference> findByUserId(Long userId);

    /**
     * Persists conversational preferences.
     *
     * @param preference preference state to persist
     * @return persisted preference state
     */
    ChatConversationPreference save(ChatConversationPreference preference);
}
