package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Component;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;

import lombok.RequiredArgsConstructor;

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
    public Optional<AppUser> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public AppUser save(AppUser user) {
        AppUserEntity saved = jpaRepository.save(toEntity(user));

        if (user.getName() != null) {
            userDetailRepository.save(UserDetailEntity.builder()
                    .appUser(saved)
                    .name(user.getName())
                    .birth(user.getBirthDate())
                    .createdAt(Instant.now())
                    .onboardingTutorialCompleted(false)
                    .themePreference(ThemePreference.LIGHT)
                    .build());
        }

        return toDomain(saved);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void addCoins(Long userId, int amount) {
        jpaRepository.addCoins(userId, amount);
    }

    @Override
    public int getCoins(Long userId) {
        return jpaRepository.findCoinsById(userId).orElse(0);
    }

    @Override
    public List<AppUser> findAllNonAdmins() {
        return jpaRepository.findByRoleNot(com.huly.backend.domain.model.enums.UserRole.ADMIN).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void saveLeadDetail(Long userId, String nickname, SourceAction sourceAction) {
        userDetailRepository.save(UserDetailEntity.builder()
                .appUser(AppUserEntity.builder().id(userId).build())
                .nickname(nickname)
                .sourceAction(sourceAction)
                .createdAt(Instant.now())
                .onboardingTutorialCompleted(false)
                .themePreference(ThemePreference.LIGHT)
                .build());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateLastLogin(Long userId) {
        jpaRepository.updateLastLogin(userId, Instant.now());
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<AppUser> findUsersInactiveSince(Instant since) {
        return jpaRepository.findByLastLoginAtBefore(since).stream()
                .map(this::toDomain)
                .toList();
    }

    private AppUser toDomain(AppUserEntity entity) {
        String name = entity.getUserDetails() != null
                ? entity.getUserDetails().stream()
                        .map(UserDetailEntity::getName)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null)
                : null;

        LocalDate birth = entity.getUserDetails() != null
                ? entity.getUserDetails().stream()
                        .map(UserDetailEntity::getBirth)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null)
                : null;

        return AppUser.builder()
                .id(entity.getId())
                .name(name)
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(entity.getRole())
                .status(entity.getStatus())
                .birthDate(birth)
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
