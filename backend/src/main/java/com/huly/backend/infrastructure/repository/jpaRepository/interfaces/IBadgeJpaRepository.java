package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.infrastructure.repository.entity.BadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface IBadgeJpaRepository extends JpaRepository<BadgeEntity, Long> {
    Optional<BadgeEntity> findByCode(String code);
    
}
