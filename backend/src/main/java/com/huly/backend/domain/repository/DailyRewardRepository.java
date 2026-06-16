package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.dailyReward.DailyReward;

import java.util.List;

public interface DailyRewardRepository {

    /** Recompensas del ciclo ordenadas por día (Día 1..N). */
    List<DailyReward> findAllOrderByDay();
}
