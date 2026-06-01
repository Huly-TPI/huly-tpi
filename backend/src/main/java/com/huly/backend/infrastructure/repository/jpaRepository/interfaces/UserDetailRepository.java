package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserDetailRepository
        extends JpaRepository<UserDetailEntity, Long> {
                Optional<UserDetailEntity> findFirstByAppUser_IdOrderByCreatedAtDesc(Long appUserId);
}