package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);

    @EntityGraph(attributePaths = {"emotions"})
    Page<ChatMessageEntity> findByChatSessionConversationIdAndChatSessionAppUserId(
            String conversationId,
            Long userId,
            Pageable pageable
    );

    Optional<ChatMessageEntity> findFirstByChatSessionAppUserIdAndRoleAndSuggestedActionEmotionalEventIdOrderByCreatedAtDesc(
            Long userId,
            MessageRole role,
            Long suggestedActionEmotionalEventId
    );

    Optional<ChatMessageEntity> findFirstByChatSessionConversationIdAndChatSessionAppUserIdAndRoleAndGeneratedChallengeTitleAndGeneratedChallengeDescriptionOrderByCreatedAtDesc(
            String conversationId,
            Long userId,
            MessageRole role,
            String generatedChallengeTitle,
            String generatedChallengeDescription
    );

    long countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(Long userId, MessageRole role, Instant since);

    long countByChatSessionAppUserIdAndRoleAndContentStartingWithAndCreatedAtAfter(
            Long userId, MessageRole role, String contentPrefix, Instant since);

    @Query("""
            SELECT m
            FROM ChatMessageEntity m
            WHERE m.chatSession.appUser.id = :userId
              AND m.role = :role
              AND m.generatedChallenge.title IS NOT NULL
            ORDER BY m.createdAt DESC
            """)
    List<ChatMessageEntity> findChallengesByUserId(
            @Param("userId") Long userId,
            @Param("role") MessageRole role,
            Pageable pageable
    );
}
