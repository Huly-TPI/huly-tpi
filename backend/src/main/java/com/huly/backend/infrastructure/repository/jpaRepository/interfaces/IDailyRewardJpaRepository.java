package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.DailyRewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IDailyRewardJpaRepository
        extends JpaRepository<DailyRewardEntity, Long> {

    List<DailyRewardEntity> findAllByOrderByDayNumberAsc();
}
