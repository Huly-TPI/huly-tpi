package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.BadgeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeRepositoryImplTest {

    @Mock
    private IBadgeJpaRepository badgeJpaRepository;

    @Mock
    private BadgeMapper badgeMapper;

    @InjectMocks
    private BadgeRepositoryImpl badgeRepositoryImpl;

    @Test
    @DisplayName("Devuelve las insignias mapeadas")
    void findAllShouldReturnMappedBadges() {
        List<BadgeEntity> entities = badgeEntities();
        givenAllBadges(entities);

        List<Badge> result = findAll();

        thenBadgesMapped(result);
    }

    @Test
    @DisplayName("Devuelve la insignia mapeada cuando existe el código")
    void findByCodeShouldReturnMappedBadgeWhenFound() {
        BadgeEntity entity = badgeEntity(1L, "PRIMER_PASO", "Primer paso");
        givenBadgeByCode(entity);

        Optional<Badge> result = findByCode("PRIMER_PASO");

        thenBadgePresent(result, entity);
    }

    @Test
    @DisplayName("Devuelve vacío cuando no existe el código")
    void findByCodeShouldReturnEmptyWhenNotFound() {
        givenBadgeByCodeNotFound("INEXISTENTE");

        Optional<Badge> result = findByCode("INEXISTENTE");

        thenBadgeAbsentForCode(result, "INEXISTENTE");
    }

    // --- arrange ---
    private void givenAllBadges(List<BadgeEntity> entities) {
        when(badgeJpaRepository.findAll()).thenReturn(entities);
        when(badgeMapper.toDomain(entities.get(0))).thenReturn(badge(1L, "PRIMER_PASO", "Primer paso"));
        when(badgeMapper.toDomain(entities.get(1))).thenReturn(badge(2L, "VALENTÍA", "Valentía"));
    }

    private void givenBadgeByCode(BadgeEntity entity) {
        when(badgeJpaRepository.findByCode("PRIMER_PASO")).thenReturn(Optional.of(entity));
        when(badgeMapper.toDomain(entity)).thenReturn(badge(1L, "PRIMER_PASO", "Primer paso"));
    }

    private void givenBadgeByCodeNotFound(String code) {
        when(badgeJpaRepository.findByCode(code)).thenReturn(Optional.empty());
    }

    private List<BadgeEntity> badgeEntities() {
        return List.of(
                badgeEntity(1L, "PRIMER_PASO", "Primer paso"),
                badgeEntity(2L, "VALENTÍA", "Valentía")
        );
    }

    private BadgeEntity badgeEntity(Long id, String code, String name) {
        return BadgeEntity.builder().id(id).code(code).name(name).build();
    }

    private Badge badge(Long id, String code, String name) {
        return Badge.builder().id(id).code(code).name(name).build();
    }

    // --- act ---
    private List<Badge> findAll() {
        return badgeRepositoryImpl.findAll();
    }

    private Optional<Badge> findByCode(String code) {
        return badgeRepositoryImpl.findByCode(code);
    }

    // --- assert ---
    private void thenBadgesMapped(List<Badge> result) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("PRIMER_PASO");
        assertThat(result.get(1).getCode()).isEqualTo("VALENTÍA");
        verify(badgeJpaRepository).findAll();
        verify(badgeMapper, times(2)).toDomain(any(BadgeEntity.class));
    }

    private void thenBadgePresent(Optional<Badge> result, BadgeEntity entity) {
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("PRIMER_PASO");
        verify(badgeJpaRepository).findByCode("PRIMER_PASO");
        verify(badgeMapper).toDomain(entity);
    }

    private void thenBadgeAbsentForCode(Optional<Badge> result, String code) {
        assertThat(result).isEmpty();
        verify(badgeJpaRepository).findByCode(code);
    }
}
