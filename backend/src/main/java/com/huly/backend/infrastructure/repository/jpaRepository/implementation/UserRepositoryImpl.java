package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final AppUserRepository jpaRepository;
    private final UserDetailRepository userDetailRepository;

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public AppUser save(AppUser user) {
        AppUserEntity saved = jpaRepository.save(toEntity(user));

        if (user.getName() != null) {
            userDetailRepository.save(UserDetailEntity.builder()
                    .appUser(saved)
                    .name(user.getName())
                    .createdAt(Instant.now())
                    .build());
        }

        return toDomain(saved);
    }

    private AppUser toDomain(AppUserEntity entity) {
        return AppUser.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(entity.getRole())
                .status(entity.getStatus())
                .build();
    }

    private AppUserEntity toEntity(AppUser domain) {
        AppUserEntity entity = new AppUserEntity();
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        entity.setRole(domain.getRole());
        entity.setStatus(domain.getStatus());
        return entity;
    }
}
