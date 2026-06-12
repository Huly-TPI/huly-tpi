package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.repository.chat.ChatConversationPreferenceRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ChatConversationPreferenceEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConversationPreferenceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter for the conversational-preference persistence port.
 */
@Component
@RequiredArgsConstructor
public class ChatConversationPreferenceRepositoryImpl implements ChatConversationPreferenceRepository {

    private final IChatConversationPreferenceJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Optional<ChatConversationPreference> findByUserId(Long userId) {
        return jpaRepository.findByAppUserId(userId).map(this::toDomain);
    }

    @Override
    public ChatConversationPreference save(ChatConversationPreference preference) {
        return toDomain(jpaRepository.save(toEntity(preference)));
    }

    private ChatConversationPreferenceEntity toEntity(ChatConversationPreference preference) {
        AppUserEntity appUser = appUserRepository.getReferenceById(preference.getUserId());
        return ChatConversationPreferenceEntity.builder()
                .id(preference.getId())
                .appUser(appUser)
                .preferredName(preference.getPreferredName())
                .communicationStyle(preference.getCommunicationStyle())
                .onboardingStatus(preference.getOnboardingStatus())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }

    private ChatConversationPreference toDomain(ChatConversationPreferenceEntity entity) {
        return ChatConversationPreference.builder()
                .id(entity.getId())
                .userId(entity.getAppUser().getId())
                .preferredName(entity.getPreferredName())
                .communicationStyle(entity.getCommunicationStyle())
                .onboardingStatus(entity.getOnboardingStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
