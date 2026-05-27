package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.chat.ChatConfig;
import com.huly.backend.infrastructure.repository.entity.ChatConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IChatConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.ChatConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatConfigRepositoryImplTest {

    @Mock private IChatConfigJpaRepository jpa;
    @Mock private ChatConfigMapper mapper;

    @InjectMocks
    private ChatConfigRepositoryImpl repository;

    @Test
    void findById_shouldReturnMappedDomain_whenEntityExists() {
        ChatConfigEntity entity = ChatConfigEntity.builder().id(1L).systemPrompt("prompt").build();
        ChatConfig domain = ChatConfig.builder().id(1L).systemPrompt("prompt").build();
        when(jpa.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<ChatConfig> result = repository.findById(1L);

        assertThat(result).contains(domain);
    }

    @Test
    void findById_shouldReturnEmpty_whenEntityNotExists() {
        when(jpa.findById(99L)).thenReturn(Optional.empty());

        Optional<ChatConfig> result = repository.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldMapSaveAndMapBack() {
        ChatConfig domain = ChatConfig.builder().id(1L).systemPrompt("p").build();
        ChatConfigEntity entity = ChatConfigEntity.builder().id(1L).systemPrompt("p").build();
        ChatConfigEntity saved = ChatConfigEntity.builder().id(1L).systemPrompt("p").build();
        ChatConfig savedDomain = ChatConfig.builder().id(1L).systemPrompt("p").build();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpa.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(savedDomain);

        ChatConfig result = repository.save(domain);

        assertThat(result).isEqualTo(savedDomain);
        verify(jpa).save(entity);
    }

    @Test
    void findFirst_shouldReturnFirstMappedDomain_whenAnyExists() {
        ChatConfigEntity entity = ChatConfigEntity.builder().id(1L).build();
        ChatConfig domain = ChatConfig.builder().id(1L).build();
        when(jpa.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<ChatConfig> result = repository.findFirst();

        assertThat(result).contains(domain);
    }

    @Test
    void findFirst_shouldReturnEmpty_whenNoEntitiesExist() {
        when(jpa.findAll()).thenReturn(List.of());

        Optional<ChatConfig> result = repository.findFirst();

        assertThat(result).isEmpty();
    }
}
