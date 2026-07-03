package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.auth.PasswordResetToken;
import com.huly.backend.domain.repository.auth.PasswordResetTokenRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.PasswordResetTokenEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final IPasswordResetTokenJpaRepository jpaRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public PasswordResetToken save(PasswordResetToken domain) {
        AppUserEntity appUser = appUserRepository.getReferenceById(domain.getUserId());
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .appUser(appUser)
                .token(domain.getToken())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getExpiresAt())
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    @Transactional
    public void delete(PasswordResetToken domain) {
        jpaRepository.deleteById(domain.getId());
    }

    @Override
    @Transactional
    public void deleteAllByUserId(Long userId) {
        AppUserEntity appUser = appUserRepository.getReferenceById(userId);
        jpaRepository.deleteAllByAppUser(appUser);
    }

    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return PasswordResetToken.builder()
                .id(entity.getId())
                .userId(entity.getAppUser().getId())
                .token(entity.getToken())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }
}
