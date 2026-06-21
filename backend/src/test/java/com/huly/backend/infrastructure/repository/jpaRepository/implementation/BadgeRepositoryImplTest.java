package com.huly.backend.infrastructure.repository.jpaRepository.implementation;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.BadgeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BadgeRepositoryImplTest {
    @Mock
    private IBadgeJpaRepository badgeJpaRepository;

    @Mock
    private BadgeMapper badgeMapper;

    @InjectMocks
    private BadgeRepositoryImpl badgeRepositoryImpl;

    @Test
    void findAll_shouldReturnMappedBadges() {
        List<BadgeEntity> entities = List.of(
                BadgeEntity.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build(),
                BadgeEntity.builder().id(2L).code("VALENTÍA").name("Valentía").build()
        );
        when(badgeJpaRepository.findAll()).thenReturn(entities);
        when(badgeMapper.toDomain(entities.get(0))).thenReturn(Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build());
        when(badgeMapper.toDomain(entities.get(1))).thenReturn(Badge.builder().id(2L).code("VALENTÍA").name("Valentía").build());

        List<Badge> result = badgeRepositoryImpl.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("PRIMER_PASO");
        assertThat(result.get(1).getCode()).isEqualTo("VALENTÍA");
        verify(badgeJpaRepository).findAll();
        verify(badgeMapper, times(2)).toDomain(any(BadgeEntity.class));
    }

    @Test
    void findByCode_shouldReturnMappedBadge_whenFound() {
        BadgeEntity entity = BadgeEntity.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build();
        when(badgeJpaRepository.findByCode("PRIMER_PASO")).thenReturn(Optional.of(entity));
        when(badgeMapper.toDomain(entity)).thenReturn(Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build());

        Optional<Badge> result = badgeRepositoryImpl.findByCode("PRIMER_PASO");

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("PRIMER_PASO");
        verify(badgeJpaRepository).findByCode("PRIMER_PASO");
        verify(badgeMapper).toDomain(entity);
    }

    @Test
    void findByCode_shouldReturnEmpty_whenNotFound() {
        when(badgeJpaRepository.findByCode("INEXISTENTE")).thenReturn(Optional.empty());
        Optional<Badge> result = badgeRepositoryImpl.findByCode("INEXISTENTE");
        assertThat(result).isEmpty();
        verify(badgeJpaRepository).findByCode("INEXISTENTE");
    }
    

    
}
