package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IChatSessionJpaRepository extends JpaRepository<ChatSessionEntity, Long> {

    Optional<ChatSessionEntity> findByConversationId(String conversationId);
}