package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.domain.model.enums.MessageRole;
import com.huly.backend.infrastructure.repository.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface IChatMessageJpaRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId);

    @EntityGraph(attributePaths = {"emotions"})
    Page<ChatMessageEntity> findByChatSessionConversationIdAndChatSessionAppUserId(
            String conversationId,
            Long userId,
            Pageable pageable
    );

    long countByChatSessionAppUserIdAndRoleAndCreatedAtAfter(Long userId, MessageRole role, Instant since);
}
