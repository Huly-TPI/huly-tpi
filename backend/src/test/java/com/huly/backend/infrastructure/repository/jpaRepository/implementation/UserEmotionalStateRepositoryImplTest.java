package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.UserEmotionalState;
import com.huly.backend.infrastructure.repository.entity.UserEmotionalStateEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserEmotionalStateJpaRepository;
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
    
    @Mock
    private IUserEmotionalStateJpaRepository jpaRepository;

    @InjectMocks
    private UserEmotionalStateRepositoryImpl repositoryImpl;

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() { 
        Instant now = Instant.now();
        UserEmotionalState domain = UserEmotionalState.builder()
            .userId(10L).valence(0.5).arousal(-0.3)
            .dominance(0.2).intensity(0.8)
            .source("chatbot").timestamp(now)
            .build();

        UserEmotionalStateEntity savedEntity = UserEmotionalStateEntity.builder()
            .id(1L).userId(10L).valence(0.5).arousal(-0.3)
            .dominance(0.2).intensity(0.8)
            .source("chatbot").timestamp(now)   
            .build();

            when(jpaRepository.save(any(UserEmotionalStateEntity.class))).thenReturn(savedEntity);

            ArgumentCaptor<UserEmotionalStateEntity> entityCaptor = ArgumentCaptor.forClass(UserEmotionalStateEntity.class);

            repositoryImpl.save(domain);

            verify(jpaRepository).save(entityCaptor.capture());
            assertThat(entityCaptor.getValue().getUserId()).isEqualTo(10L);
            assertThat(entityCaptor.getValue().getSource()).isEqualTo("chatbot");
            assertThat(entityCaptor.getValue().getId()).isNull(); 
            
    }

    @Test 
    void save_shouldMapEntityToDomainAfterPersisting() { 
        Instant now = Instant.now();
        UserEmotionalStateEntity savedEntity = UserEmotionalStateEntity.builder()
            .id(1L).userId(10L).valence(0.5).arousal(-0.3)
            .dominance(0.2).intensity(0.8)
            .source("chatbot").timestamp(now)   
            .build();

        when(jpaRepository.save(any(UserEmotionalStateEntity.class))).thenReturn(savedEntity);

        UserEmotionalState result = repositoryImpl.save(
            UserEmotionalState.builder()
            .userId(10L).valence(0.5).arousal(-0.3)
            .dominance(0.2).intensity(0.8)
            .source("chatbot").timestamp(now)
            .build()
        );

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getSource()).isEqualTo("chatbot");
    }
}
