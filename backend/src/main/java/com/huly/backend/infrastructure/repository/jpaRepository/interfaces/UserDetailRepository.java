package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository
        extends JpaRepository<UserDetailEntity, Long> {
}