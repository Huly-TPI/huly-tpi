package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.extension.AntiScrollGlobalConfig;
import com.huly.backend.infrastructure.repository.entity.AntiScrollConfigEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IAntiScrollConfigJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.AntiScrollGlobalConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AntiScrollGlobalConfigRepositoryImplTest {

    @Mock private IAntiScrollConfigJpaRepository jpa;
    @Mock private AntiScrollGlobalConfigMapper mapper;

    @InjectMocks
    private AntiScrollGlobalConfigRepositoryImpl repository;

    @Test
    void save_shouldMapSaveAndMapBack() {
        AntiScrollGlobalConfig domain = AntiScrollGlobalConfig.builder().id(1L).defaultPauseIntervalMinutes(15).build();
        AntiScrollConfigEntity entity = AntiScrollConfigEntity.builder().id(1L).defaultPauseIntervalMinutes(15).build();
        AntiScrollConfigEntity saved = AntiScrollConfigEntity.builder().id(1L).defaultPauseIntervalMinutes(15).build();
        AntiScrollGlobalConfig savedDomain = AntiScrollGlobalConfig.builder().id(1L).defaultPauseIntervalMinutes(15).build();

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpa.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(savedDomain);

        AntiScrollGlobalConfig result = repository.save(domain);

        assertThat(result).isEqualTo(savedDomain);
        verify(jpa).save(entity);
    }

    @Test
    void findFirst_shouldReturnFirstMappedDomain_whenAnyExists() {
        AntiScrollConfigEntity entity = AntiScrollConfigEntity.builder().id(1L).build();
        AntiScrollGlobalConfig domain = AntiScrollGlobalConfig.builder().id(1L).build();
        when(jpa.findAll()).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<AntiScrollGlobalConfig> result = repository.findFirst();

        assertThat(result).contains(domain);
    }

    @Test
    void findFirst_shouldReturnEmpty_whenNoEntitiesExist() {
        when(jpa.findAll()).thenReturn(List.of());

        Optional<AntiScrollGlobalConfig> result = repository.findFirst();

        assertThat(result).isEmpty();
    }
}
