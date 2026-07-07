package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.PasswordResetTokenEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPasswordResetTokenJpaRepository;
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
class PasswordResetTokenRepositoryImplTest {

    private static final Long USER_ID = 4L;
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-01-01T11:00:00Z");

    @Mock
    private IPasswordResetTokenJpaRepository jpaRepository;
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private PasswordResetTokenRepositoryImpl repository;

    @Test
    @DisplayName("Construye la entidad con el usuario referenciado al guardar")
    void saveShouldBuildEntityWithReferencedUser() {
        givenReferencedUser();
        givenSaved(persistedToken(1L));

        save(domainToken());

        thenPersistedEntityMatches();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio al guardar")
    void saveShouldMapPersistedEntityToDomain() {
        givenReferencedUser();
        givenSaved(persistedToken(1L));

        PasswordResetToken result = save(domainToken());

        thenTokenMatches(result, 1L);
    }

    @Test
    @DisplayName("Devuelve el token mapeado cuando existe")
    void findByTokenShouldReturnMappedTokenWhenPresent() {
        givenTokenByValue("tok-123", persistedToken(1L));

        Optional<PasswordResetToken> result = findByToken("tok-123");

        thenTokenPresent(result, "tok-123");
    }

    @Test
    @DisplayName("Devuelve vacío cuando el token no existe")
    void findByTokenShouldReturnEmptyWhenAbsent() {
        givenTokenByValue("missing", null);

        Optional<PasswordResetToken> result = findByToken("missing");

        thenAbsent(result);
    }

    @Test
    @DisplayName("Elimina el token delegando por id")
    void deleteShouldDelegateById() {
        delete(tokenWithId(9L));

        thenDeletedById(9L);
    }

    @Test
    @DisplayName("Elimina todos los tokens del usuario referenciado")
    void deleteAllByUserIdShouldDeleteByReferencedUser() {
        givenReferencedUser();

        deleteAllByUserId(USER_ID);

        thenDeletedAllByReferencedUser();
    }

    // --- arrange ---
    private void givenReferencedUser() {
        when(appUserRepository.getReferenceById(USER_ID)).thenReturn(appUserEntity(USER_ID));
    }

    private void givenSaved(PasswordResetTokenEntity entity) {
        when(jpaRepository.save(any())).thenReturn(entity);
    }

    private void givenTokenByValue(String token, PasswordResetTokenEntity entity) {
        when(jpaRepository.findByToken(token)).thenReturn(Optional.ofNullable(entity));
    }

    private PasswordResetToken domainToken() {
        return PasswordResetToken.builder()
                .userId(USER_ID)
                .token("tok-123")
                .createdAt(CREATED_AT)
                .expiresAt(EXPIRES_AT)
                .build();
    }

    private PasswordResetToken tokenWithId(Long id) {
        return PasswordResetToken.builder().id(id).build();
    }

    private PasswordResetTokenEntity persistedToken(Long id) {
        return PasswordResetTokenEntity.builder()
                .id(id)
                .appUser(appUserEntity(USER_ID))
                .token("tok-123")
                .createdAt(CREATED_AT)
                .expiresAt(EXPIRES_AT)
                .build();
    }

    private AppUserEntity appUserEntity(Long id) {
        AppUserEntity entity = new AppUserEntity();
        entity.setId(id);
        return entity;
    }

    // --- act ---
    private PasswordResetToken save(PasswordResetToken domain) {
        return repository.save(domain);
    }

    private Optional<PasswordResetToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    private void delete(PasswordResetToken domain) {
        repository.delete(domain);
    }

    private void deleteAllByUserId(Long userId) {
        repository.deleteAllByUserId(userId);
    }

    // --- assert ---
    private void thenPersistedEntityMatches() {
        ArgumentCaptor<PasswordResetTokenEntity> captor = ArgumentCaptor.forClass(PasswordResetTokenEntity.class);
        verify(jpaRepository).save(captor.capture());
        PasswordResetTokenEntity persisted = captor.getValue();
        assertThat(persisted.getAppUser().getId()).isEqualTo(USER_ID);
        assertThat(persisted.getToken()).isEqualTo("tok-123");
        assertThat(persisted.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(persisted.getExpiresAt()).isEqualTo(EXPIRES_AT);
    }

    private void thenTokenMatches(PasswordResetToken result, Long expectedId) {
        assertThat(result.getId()).isEqualTo(expectedId);
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getToken()).isEqualTo("tok-123");
        assertThat(result.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(result.getExpiresAt()).isEqualTo(EXPIRES_AT);
    }

    private void thenTokenPresent(Optional<PasswordResetToken> result, String token) {
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token);
    }

    private void thenAbsent(Optional<PasswordResetToken> result) {
        assertThat(result).isEmpty();
    }

    private void thenDeletedById(Long id) {
        verify(jpaRepository).deleteById(id);
    }

    private void thenDeletedAllByReferencedUser() {
        ArgumentCaptor<AppUserEntity> captor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(jpaRepository).deleteAllByAppUser(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(USER_ID);
    }
}
