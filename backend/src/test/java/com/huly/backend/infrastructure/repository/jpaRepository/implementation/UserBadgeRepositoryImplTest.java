package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.model.user.UserBadge;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import com.huly.backend.infrastructure.repository.entity.UserBadgeEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserBadgeJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.UserBadgeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBadgeRepositoryImplTest {

    private static final Instant NOW = Instant.now();

    @InjectMocks
    private UserBadgeRepositoryImpl userBadgeRepository;

    @Mock
    private IUserBadgeJpaRepository userBadgeJpaRepository;
    @Mock
    private UserBadgeMapper userBadgeMapper;
    @Mock
    private IBadgeJpaRepository iBadgeJpaRepository;

    @Test
    @DisplayName("Mapea a dominio los badges del usuario")
    void findAllByUserIdShouldReturnMappedList() {
        UserBadgeEntity entity = emptyBadgeEntity();
        givenBadgesForUser(1L, entity);
        givenMappedBadge(entity, mappedFirstBadge(entity));

        List<UserBadge> result = findAllByUserId(1L);

        thenBadgeListMatches(result);
        thenBadgesQueried(1L);
        thenBadgeMapped(entity);
    }

    @Test
    @DisplayName("Devuelve verdadero cuando el usuario tiene el badge")
    void existsByUserIdAndBadgeCodeShouldReturnTrue() {
        givenBadgeExists(1L, "PRIMER_PASO", true);

        boolean result = existsByUserIdAndBadgeCode(1L, "PRIMER_PASO");

        thenTrue(result);
        thenExistenceQueried(1L, "PRIMER_PASO");
    }

    @Test
    @DisplayName("Devuelve falso cuando el usuario no tiene el badge")
    void existsByUserIdAndBadgeCodeShouldReturnFalseWhenNotExists() {
        givenBadgeExists(1L, "INEXISTENTE", false);

        boolean result = existsByUserIdAndBadgeCode(1L, "INEXISTENTE");

        thenFalse(result);
        thenExistenceQueried(1L, "INEXISTENTE");
    }

    @Test
    @DisplayName("Construye la entidad con los ids de usuario y badge al guardar")
    void saveShouldBuildEntityWithCorrectUserAndBadgeIds() {
        UserBadgeEntity savedEntity = savedBadgeEntity();
        givenBadgeReference(2L, "VALENTÍA");
        givenSaved(savedEntity);
        givenMappedBadge(savedEntity, mappedSavedBadge());

        UserBadge result = save(badgeToSave());

        thenPersistedEntityMatches();
        thenSavedBadgeMatches(result);
    }

    // --- arrange ---
    private void givenBadgesForUser(Long userId, UserBadgeEntity entity) {
        when(userBadgeJpaRepository.findAllByUserId(userId)).thenReturn(List.of(entity));
    }

    private void givenMappedBadge(UserBadgeEntity entity, UserBadge domain) {
        when(userBadgeMapper.toDomain(entity)).thenReturn(domain);
    }

    private void givenBadgeExists(Long userId, String badgeCode, boolean exists) {
        when(userBadgeJpaRepository.existsByUserIdAndBadgeCode(userId, badgeCode)).thenReturn(exists);
    }

    private void givenBadgeReference(Long badgeId, String code) {
        when(iBadgeJpaRepository.getReferenceById(badgeId))
                .thenReturn(BadgeEntity.builder().id(badgeId).code(code).build());
    }

    private void givenSaved(UserBadgeEntity entity) {
        when(userBadgeJpaRepository.save(any(UserBadgeEntity.class))).thenReturn(entity);
    }

    private UserBadgeEntity emptyBadgeEntity() {
        return UserBadgeEntity.builder().id(1L).build();
    }

    private UserBadgeEntity savedBadgeEntity() {
        return UserBadgeEntity.builder().id(10L).build();
    }

    private UserBadge mappedFirstBadge(UserBadgeEntity entity) {
        return UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build())
                .obtainedAt(entity.getObtainedAt())
                .build();
    }

    private Badge valentiaBadge() {
        return Badge.builder().id(2L).code("VALENTÍA").name("Valentía").build();
    }

    private UserBadge badgeToSave() {
        return UserBadge.builder()
                .id(1L)
                .userId(1L)
                .badge(valentiaBadge())
                .obtainedAt(NOW)
                .build();
    }

    private UserBadge mappedSavedBadge() {
        return UserBadge.builder()
                .id(10L)
                .userId(1L)
                .badge(valentiaBadge())
                .obtainedAt(NOW)
                .build();
    }

    // --- act ---
    private List<UserBadge> findAllByUserId(Long userId) {
        return userBadgeRepository.findAllByUserId(userId);
    }

    private boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode) {
        return userBadgeRepository.existsByUserIdAndBadgeCode(userId, badgeCode);
    }

    private UserBadge save(UserBadge userBadge) {
        return userBadgeRepository.save(userBadge);
    }

    // --- assert ---
    private void thenBadgeListMatches(List<UserBadge> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getBadge().getCode()).isEqualTo("PRIMER_PASO");
    }

    private void thenBadgesQueried(Long userId) {
        verify(userBadgeJpaRepository).findAllByUserId(userId);
    }

    private void thenBadgeMapped(UserBadgeEntity entity) {
        verify(userBadgeMapper).toDomain(entity);
    }

    private void thenTrue(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenFalse(boolean result) {
        assertThat(result).isFalse();
    }

    private void thenExistenceQueried(Long userId, String badgeCode) {
        verify(userBadgeJpaRepository).existsByUserIdAndBadgeCode(userId, badgeCode);
    }

    private void thenPersistedEntityMatches() {
        ArgumentCaptor<UserBadgeEntity> captor = ArgumentCaptor.forClass(UserBadgeEntity.class);
        verify(userBadgeJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getBadge().getCode()).isEqualTo("VALENTÍA");
    }

    private void thenSavedBadgeMatches(UserBadge result) {
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBadge().getCode()).isEqualTo("VALENTÍA");
        assertThat(result.getObtainedAt()).isEqualTo(NOW);
    }
}
