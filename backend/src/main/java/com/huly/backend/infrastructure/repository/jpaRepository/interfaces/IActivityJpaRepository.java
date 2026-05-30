package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.infrastructure.repository.entity.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IActivityJpaRepository extends JpaRepository<ActivityEntity, Long> {
    
}
