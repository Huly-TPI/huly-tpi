package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.repository.DailyRewardRepository;
import com.huly.backend.infrastructure.repository.entity.DailyRewardEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IDailyRewardJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyRewardRepositoryImpl implements DailyRewardRepository {

    private final IDailyRewardJpaRepository jpaRepository;

    @Override
    public List<DailyReward> findAllOrderByDay() {
        return jpaRepository.findAllByOrderByDayNumberAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    private DailyReward toDomain(DailyRewardEntity entity) {
        return DailyReward.builder()
                .id(entity.getId())
                .dayNumber(entity.getDayNumber())
                .coins(entity.getCoins())
                .build();
    }
}
