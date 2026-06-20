package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.auth.RefreshToken;
import com.huly.backend.domain.repository.auth.RefreshTokenRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.RefreshTokenEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IRefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final IRefreshTokenJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public RefreshToken save(RefreshToken domain) {
        AppUserEntity appUser = appUserRepository.getReferenceById(domain.getUserId());

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .appUser(appUser)
                .token(domain.getToken())
                .createdAt(domain.getCreatedAt())
                .expiredAt(domain.getExpiredAt())
                .build();

        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public void delete(RefreshToken domain) {
        jpaRepository.deleteById(domain.getId());
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        return RefreshToken.builder()
                .id(entity.getId())
                .userId(entity.getAppUser().getId())
                .token(entity.getToken())
                .createdAt(entity.getCreatedAt())
                .expiredAt(entity.getExpiredAt())
                .build();
    }
}
