package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.RefreshTokenEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IRefreshTokenJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RefreshTokenRepositoryImplTest {

    private static final Instant NOW = Instant.now();

    @Mock private IRefreshTokenJpaRepository jpaRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks private RefreshTokenRepositoryImpl refreshTokenRepository;

    @Test
    @DisplayName("Mapea el dominio a entidad y devuelve el resultado mapeado al guardar")
    void saveShouldMapDomainToEntityAndReturnMappedResult() {
        givenReferencedUser(1L);
        givenSaved(savedTokenEntity());

        RefreshToken result = save(domainToken());

        thenSavedTokenMatches(result);
    }

    @Test
    @DisplayName("Devuelve el token por valor cuando existe")
    void findByTokenShouldReturnMappedDomainWhenFound() {
        givenTokenFound("someToken", foundTokenEntity());

        Optional<RefreshToken> result = findByToken("someToken");

        thenTokenPresent(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar el token cuando no existe")
    void findByTokenShouldReturnEmptyWhenNotFound() {
        givenTokenFound("missing", null);

        Optional<RefreshToken> result = findByToken("missing");

        thenEmpty(result);
    }

    @Test
    @DisplayName("Elimina el token por id")
    void deleteShouldCallDeleteByIdWithCorrectId() {
        delete(tokenToDelete());

        thenDeletedById(7L);
    }

    // --- arrange ---
    private void givenReferencedUser(Long userId) {
        when(appUserRepository.getReferenceById(userId)).thenReturn(userEntity(userId));
    }

    private void givenSaved(RefreshTokenEntity entity) {
        when(jpaRepository.save(any())).thenReturn(entity);
    }

    private void givenTokenFound(String token, RefreshTokenEntity entity) {
        when(jpaRepository.findByToken(token)).thenReturn(Optional.ofNullable(entity));
    }

    private RefreshToken domainToken() {
        return RefreshToken.builder()
                .userId(1L).token("tok")
                .createdAt(NOW).expiredAt(NOW.plusSeconds(3600)).build();
    }

    private RefreshToken tokenToDelete() {
        return RefreshToken.builder().id(7L).build();
    }

    private RefreshTokenEntity savedTokenEntity() {
        return RefreshTokenEntity.builder()
                .id(10L).appUser(userEntity(1L)).token("tok")
                .createdAt(NOW).expiredAt(NOW.plusSeconds(3600)).build();
    }

    private RefreshTokenEntity foundTokenEntity() {
        return RefreshTokenEntity.builder()
                .id(5L).appUser(userEntity(2L)).token("someToken")
                .createdAt(NOW).expiredAt(NOW.plusSeconds(3600)).build();
    }

    private AppUserEntity userEntity(Long id) {
        return AppUserEntity.builder().id(id).build();
    }

    // --- act ---
    private RefreshToken save(RefreshToken domain) {
        return refreshTokenRepository.save(domain);
    }

    private Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    private void delete(RefreshToken domain) {
        refreshTokenRepository.delete(domain);
    }

    // --- assert ---
    private void thenSavedTokenMatches(RefreshToken result) {
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getToken()).isEqualTo("tok");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isEqualTo(NOW);
    }

    private void thenTokenPresent(Optional<RefreshToken> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        assertThat(result.get().getToken()).isEqualTo("someToken");
        assertThat(result.get().getUserId()).isEqualTo(2L);
    }

    private void thenEmpty(Optional<RefreshToken> result) {
        assertThat(result).isEmpty();
    }

    private void thenDeletedById(Long id) {
        verify(jpaRepository).deleteById(id);
    }
}
