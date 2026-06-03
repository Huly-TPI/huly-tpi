package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.repository.chat.ChatSessionRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ChatSessionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatSessionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final IChatSessionJpaRepository jpa;
    private final AppUserRepository appUserRepository;

    @Override
    public Optional<String> findConversationIdBySessionId(Long sessionId) {
        return jpa.findById(sessionId).map(ChatSessionEntity::getConversationId);
    }

    @Override
    public Long saveSession(String conversationId, Long userId) {
        AppUserEntity user = userId != null
                ? appUserRepository.findById(userId).orElse(null)
                : null;
        ChatSessionEntity entity = ChatSessionEntity.builder()
                .conversationId(conversationId)
                .appUser(user)
                .startAt(Instant.now())
                .build();
        return jpa.save(entity).getId();
    }

    @Override
    public Optional<Long> findSessionIdByConversationIdAndUserId(String conversationId, Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        return jpa.findByConversationIdAndAppUserId(conversationId, userId)
                .map(ChatSessionEntity::getId);
    }
}
