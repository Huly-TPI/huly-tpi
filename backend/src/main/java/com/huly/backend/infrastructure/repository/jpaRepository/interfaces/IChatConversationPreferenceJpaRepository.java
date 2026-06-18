package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.ChatConversationPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for conversational preferences.
 */
public interface IChatConversationPreferenceJpaRepository
        extends JpaRepository<ChatConversationPreferenceEntity, Long> {

    /**
     * Finds the preference row owned by one user.
     *
     * @param userId user identifier
     * @return stored entity when present
     */
    Optional<ChatConversationPreferenceEntity> findByAppUserId(Long userId);
}
