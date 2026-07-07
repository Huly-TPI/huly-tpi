package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.infrastructure.repository.entity.DailyRewardEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IDailyRewardJpaRepository;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Mapea las entidades a dominio ordenadas por día")
    void findAllOrderByDayShouldMapEntitiesToDomain() {
        givenRewards(rewardEntity(1L, 1, 10), rewardEntity(2L, 2, 15));

        List<DailyReward> result = findAllOrderByDay();

        thenRewardsMapped(result);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay filas")
    void findAllOrderByDayShouldReturnEmptyWhenNoRows() {
        givenRewards();

        List<DailyReward> result = findAllOrderByDay();

        thenEmpty(result);
    }

    // --- arrange ---
    private void givenRewards(DailyRewardEntity... entities) {
        when(jpaRepository.findAllByOrderByDayNumberAsc()).thenReturn(List.of(entities));
    }

    private DailyRewardEntity rewardEntity(Long id, int dayNumber, int coins) {
        return DailyRewardEntity.builder().id(id).dayNumber(dayNumber).coins(coins).build();
    }

    // --- act ---
    private List<DailyReward> findAllOrderByDay() {
        return repository.findAllOrderByDay();
    }

    // --- assert ---
    private void thenRewardsMapped(List<DailyReward> result) {
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getDayNumber()).isEqualTo(1);
        assertThat(result.get(0).getCoins()).isEqualTo(10);
        assertThat(result.get(1).getDayNumber()).isEqualTo(2);
        assertThat(result.get(1).getCoins()).isEqualTo(15);
    }

    private void thenEmpty(List<DailyReward> result) {
        assertThat(result).isEmpty();
    }
}
