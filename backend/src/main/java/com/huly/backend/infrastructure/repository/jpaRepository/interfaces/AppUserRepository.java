package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;


import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
    boolean existsByEmail(String email);
}

