package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;


import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {

    boolean existsByEmail(String email);

    Optional<AppUserEntity> findByEmail(String email);
}