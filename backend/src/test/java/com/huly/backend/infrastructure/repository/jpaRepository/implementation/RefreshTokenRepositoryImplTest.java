package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.RefreshTokenEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IRefreshTokenJpaRepository;
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

    @Mock private IRefreshTokenJpaRepository jpaRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks private RefreshTokenRepositoryImpl refreshTokenRepository;

    @Test
    void save_shouldMapDomainToEntityAndReturnMappedResult() {
        Instant now = Instant.now();
        AppUserEntity userEntity = AppUserEntity.builder().id(1L).build();
        RefreshTokenEntity savedEntity = RefreshTokenEntity.builder()
                .id(10L).appUser(userEntity).token("tok")
                .createdAt(now).expiredAt(now.plusSeconds(3600)).build();

        when(appUserRepository.getReferenceById(1L)).thenReturn(userEntity);
        when(jpaRepository.save(any())).thenReturn(savedEntity);

        RefreshToken domain = RefreshToken.builder()
                .userId(1L).token("tok")
                .createdAt(now).expiredAt(now.plusSeconds(3600)).build();

        RefreshToken result = refreshTokenRepository.save(domain);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getToken()).isEqualTo("tok");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void findByToken_shouldReturnMappedDomain_whenFound() {
        Instant now = Instant.now();
        AppUserEntity userEntity = AppUserEntity.builder().id(2L).build();
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(5L).appUser(userEntity).token("someToken")
                .createdAt(now).expiredAt(now.plusSeconds(3600)).build();
        when(jpaRepository.findByToken("someToken")).thenReturn(Optional.of(entity));

        Optional<RefreshToken> result = refreshTokenRepository.findByToken("someToken");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        assertThat(result.get().getToken()).isEqualTo("someToken");
        assertThat(result.get().getUserId()).isEqualTo(2L);
    }

    @Test
    void findByToken_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThat(refreshTokenRepository.findByToken("missing")).isEmpty();
    }

    @Test
    void delete_shouldCallDeleteByIdWithCorrectId() {
        RefreshToken domain = RefreshToken.builder().id(7L).build();

        refreshTokenRepository.delete(domain);

        verify(jpaRepository).deleteById(7L);
    }
}
