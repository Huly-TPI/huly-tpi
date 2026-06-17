package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.infrastructure.repository.entity.DailyRewardEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IDailyRewardJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyRewardRepositoryImplTest {

    @Mock
    private IDailyRewardJpaRepository jpaRepository;

    @InjectMocks
    private DailyRewardRepositoryImpl repository;

    @Test
    void findAllOrderByDay_shouldMapEntitiesToDomain() {
        when(jpaRepository.findAllByOrderByDayNumberAsc()).thenReturn(List.of(
                DailyRewardEntity.builder().id(1L).dayNumber(1).coins(10).build(),
                DailyRewardEntity.builder().id(2L).dayNumber(2).coins(15).build()
        ));

        List<DailyReward> result = repository.findAllOrderByDay();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getDayNumber()).isEqualTo(1);
        assertThat(result.get(0).getCoins()).isEqualTo(10);
        assertThat(result.get(1).getDayNumber()).isEqualTo(2);
        assertThat(result.get(1).getCoins()).isEqualTo(15);
    }

    @Test
    void findAllOrderByDay_shouldReturnEmpty_whenNoRows() {
        when(jpaRepository.findAllByOrderByDayNumberAsc()).thenReturn(List.of());

        assertThat(repository.findAllOrderByDay()).isEmpty();
    }
}
