package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.user.UserEmotionalState;
import com.huly.backend.infrastructure.repository.entity.UserEmotionalStateEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserEmotionalStateJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEmotionalStateRepositoryImplTest {

    private static final Long USER_ID = 10L;
    private static final Long PERSISTED_ID = 1L;
    private static final String SOURCE = "chatbot";
    private static final Instant TIMESTAMP = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private IUserEmotionalStateJpaRepository jpaRepository;

    @InjectMocks
    private UserEmotionalStateRepositoryImpl repositoryImpl;

    @Test
    @DisplayName("Mapea el dominio a entidad antes de persistir")
    void saveShouldMapDomainToEntityBeforePersisting() {
        givenSaved(persistedState());

        save(domainState());

        thenPersistedEntityMatches();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio después de persistir")
    void saveShouldMapEntityToDomainAfterPersisting() {
        givenSaved(persistedState());

        UserEmotionalState result = save(domainState());

        thenStateMatches(result);
    }

    // --- arrange ---
    private void givenSaved(UserEmotionalStateEntity entity) {
        when(jpaRepository.save(any(UserEmotionalStateEntity.class))).thenReturn(entity);
    }

    private UserEmotionalState domainState() {
        return UserEmotionalState.builder()
                .userId(USER_ID).valence(0.5).arousal(-0.3)
                .dominance(0.2).intensity(0.8)
                .source(SOURCE).timestamp(TIMESTAMP)
                .build();
    }

    private UserEmotionalStateEntity persistedState() {
        return UserEmotionalStateEntity.builder()
                .id(PERSISTED_ID).userId(USER_ID).valence(0.5).arousal(-0.3)
                .dominance(0.2).intensity(0.8)
                .source(SOURCE).timestamp(TIMESTAMP)
                .build();
    }

    // --- act ---
    private UserEmotionalState save(UserEmotionalState domain) {
        return repositoryImpl.save(domain);
    }

    // --- assert ---
    private void thenPersistedEntityMatches() {
        ArgumentCaptor<UserEmotionalStateEntity> captor = ArgumentCaptor.forClass(UserEmotionalStateEntity.class);
        verify(jpaRepository).save(captor.capture());
        UserEmotionalStateEntity persisted = captor.getValue();
        assertThat(persisted.getUserId()).isEqualTo(USER_ID);
        assertThat(persisted.getSource()).isEqualTo(SOURCE);
        assertThat(persisted.getId()).isNull();
    }

    private void thenStateMatches(UserEmotionalState result) {
        assertThat(result.getId()).isEqualTo(PERSISTED_ID);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getSource()).isEqualTo(SOURCE);
    }
}
