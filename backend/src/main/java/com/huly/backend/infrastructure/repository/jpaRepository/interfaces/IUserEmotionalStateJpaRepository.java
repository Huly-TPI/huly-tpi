package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.infrastructure.repository.entity.UserEmotionalStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserEmotionalStateJpaRepository extends JpaRepository<UserEmotionalStateEntity, Long> {
    
}
