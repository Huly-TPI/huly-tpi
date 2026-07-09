package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatConversationPreference;
import com.huly.backend.domain.model.enums.ChatOnboardingStatus;
import com.huly.backend.domain.model.enums.CommunicationStyle;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.ChatConversationPreferenceEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConversationPreferenceJpaRepository;
import org.junit.jupiter.api.DisplayName;
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

    private static final Long USER_ID = 7L;
    private static final Long PREFERENCE_ID = 3L;
    private static final String PREFERRED_NAME = "Checho";

    @Mock
    private IChatConversationPreferenceJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @InjectMocks
    private ChatConversationPreferenceRepositoryImpl repository;

    @Test
    @DisplayName("Mapea el dominio y devuelve la preferencia persistida al guardar")
    void saveShouldMapDomainAndReturnPersistedPreference() {
        givenReferencedUser();
        givenSaveAssignsId(PREFERENCE_ID);

        ChatConversationPreference result = save(domainPreference());

        thenSavedPreferenceMappedFromDomain();
        thenPersistedPreference(result);
    }

    @Test
    @DisplayName("Mapea la entidad almacenada al buscar por userId")
    void findByUserIdShouldMapStoredEntity() {
        givenStoredPreference(storedEntity());

        Optional<ChatConversationPreference> result = findByUserId();

        thenStoredPreferenceMapped(result);
    }

    // --- arrange ---
    private void givenReferencedUser() {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(AppUserEntity.builder().id(USER_ID).build());
    }

    private void givenSaveAssignsId(Long id) {
        when(jpaRepository.save(any())).thenAnswer(invocation -> {
            ChatConversationPreferenceEntity entity = invocation.getArgument(0);
            entity.setId(id);
            return entity;
        });
    }

    private void givenStoredPreference(ChatConversationPreferenceEntity entity) {
        when(jpaRepository.findByAppUserId(USER_ID)).thenReturn(Optional.of(entity));
    }

    private ChatConversationPreference domainPreference() {
        Instant now = Instant.parse("2026-06-10T12:00:00Z");
        return ChatConversationPreference.builder()
                .userId(USER_ID)
                .preferredName(PREFERRED_NAME)
                .communicationStyle(CommunicationStyle.DIRECT)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private ChatConversationPreferenceEntity storedEntity() {
        return ChatConversationPreferenceEntity.builder()
                .id(PREFERENCE_ID)
                .appUser(AppUserEntity.builder().id(USER_ID).build())
                .preferredName(PREFERRED_NAME)
                .communicationStyle(CommunicationStyle.FRIEND_LIKE)
                .onboardingStatus(ChatOnboardingStatus.COMPLETED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // --- act ---
    private ChatConversationPreference save(ChatConversationPreference preference) {
        return repository.save(preference);
    }

    private Optional<ChatConversationPreference> findByUserId() {
        return repository.findByUserId(USER_ID);
    }

    // --- assert ---
    private void thenSavedPreferenceMappedFromDomain() {
        ArgumentCaptor<ChatConversationPreferenceEntity> captor =
                ArgumentCaptor.forClass(ChatConversationPreferenceEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getAppUser().getId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getPreferredName()).isEqualTo(PREFERRED_NAME);
    }

    private void thenPersistedPreference(ChatConversationPreference result) {
        assertThat(result.getId()).isEqualTo(PREFERENCE_ID);
        assertThat(result.getCommunicationStyle()).isEqualTo(CommunicationStyle.DIRECT);
    }

    private void thenStoredPreferenceMapped(Optional<ChatConversationPreference> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(USER_ID);
        assertThat(result.get().getPreferredName()).isEqualTo(PREFERRED_NAME);
        assertThat(result.get().getCommunicationStyle()).isEqualTo(CommunicationStyle.FRIEND_LIKE);
    }
}
