package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserBadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IUserBadgeJpaRepository extends JpaRepository<UserBadgeEntity, Long> {
    List<UserBadgeEntity> findAllByUserId(Long userId);
    boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode);
    
}
