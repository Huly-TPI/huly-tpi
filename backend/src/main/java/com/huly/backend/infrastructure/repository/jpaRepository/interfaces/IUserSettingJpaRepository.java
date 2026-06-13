package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IUserSettingJpaRepository extends JpaRepository<UserSettingEntity, Long> {
    Optional<UserSettingEntity> findByAppUser_Id(Long userId);
}
