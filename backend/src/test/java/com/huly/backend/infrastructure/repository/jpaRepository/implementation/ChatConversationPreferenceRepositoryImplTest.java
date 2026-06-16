package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ChatConversationPreferenceEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConversationPreferenceJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatConversationPreferenceRepositoryImplTest {

    @Mock
    private IChatConversationPreferenceJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @InjectMocks
    private ChatConversationPreferenceRepositoryImpl repository;

    @Test
    void save_shouldMapDomainAndReturnPersistedPreference() {
        Instant now = Instant.parse("2026-06-10T12:00:00Z");
        ChatConversationPreference preference = ChatConversationPreference.builder()
                .userId(7L)
                .preferredName("Checho")
                .communicationStyle(CommunicationStyle.DIRECT)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .createdAt(now)
                .updatedAt(now)
                .build();
        AppUserEntity user = AppUserEntity.builder().id(7L).build();
        when(appUserRepository.getReferenceById(7L)).thenReturn(user);
        when(jpaRepository.save(any())).thenAnswer(invocation -> {
            ChatConversationPreferenceEntity entity = invocation.getArgument(0);
            entity.setId(3L);
            return entity;
        });

        ChatConversationPreference result = repository.save(preference);

        ArgumentCaptor<ChatConversationPreferenceEntity> captor =
                ArgumentCaptor.forClass(ChatConversationPreferenceEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getAppUser().getId()).isEqualTo(7L);
        assertThat(captor.getValue().getPreferredName()).isEqualTo("Checho");
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getCommunicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
    }

    @Test
    void findByUserId_shouldMapStoredEntity() {
        ChatConversationPreferenceEntity entity = ChatConversationPreferenceEntity.builder()
                .id(3L)
                .appUser(AppUserEntity.builder().id(7L).build())
                .preferredName("Checho")
                .communicationStyle(CommunicationStyle.FRIEND_LIKE)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        when(jpaRepository.findByAppUserId(7L)).thenReturn(Optional.of(entity));

        Optional<ChatConversationPreference> result = repository.findByUserId(7L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(7L);
        assertThat(result.get().getPreferredName()).isEqualTo("Checho");
        assertThat(result.get().getCommunicationStyle()).isEqualTo(CommunicationStyle.FRIEND_LIKE);
    }
}
