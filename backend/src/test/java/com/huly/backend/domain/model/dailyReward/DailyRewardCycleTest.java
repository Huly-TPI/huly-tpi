package com.huly.backend.domain.model.dailyReward;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DailyRewardCycleTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    private static List<DailyReward> sevenDayCycle() {
        int[] coins = {10, 15, 20, 25, 30, 40, 100};
        return java.util.stream.IntStream.rangeClosed(1, 7)
                .mapToObj(d -> DailyReward.builder().id((long) d).dayNumber(d).coins(coins[d - 1]).build())
                .toList();
    }

    // --- isAlive ---

    @Test
    void isAlive_shouldBeFalse_whenNeverClaimed() {
        assertThat(DailyRewardCycle.isAlive(new DailyClaimState(0, null), TODAY)).isFalse();
    }

    @Test
    void isAlive_shouldBeTrue_whenClaimedToday() {
        assertThat(DailyRewardCycle.isAlive(new DailyClaimState(3, TODAY), TODAY)).isTrue();
    }

    @Test
    void isAlive_shouldBeTrue_whenClaimedYesterday() {
        assertThat(DailyRewardCycle.isAlive(new DailyClaimState(3, TODAY.minusDays(1)), TODAY)).isTrue();
    }

    @Test
    void isAlive_shouldBeFalse_whenClaimedTwoDaysAgo() {
        assertThat(DailyRewardCycle.isAlive(new DailyClaimState(3, TODAY.minusDays(2)), TODAY)).isFalse();
    }

    // --- nextStreak ---

    @Test
    void nextStreak_shouldIncrement_whenConsecutive() {
        assertThat(DailyRewardCycle.nextStreak(new DailyClaimState(3, TODAY.minusDays(1)), TODAY)).isEqualTo(4);
    }

    @Test
    void nextStreak_shouldResetToOne_whenFirstClaim() {
        assertThat(DailyRewardCycle.nextStreak(new DailyClaimState(0, null), TODAY)).isEqualTo(1);
    }

    @Test
    void nextStreak_shouldResetToOne_whenADayWasMissed() {
        assertThat(DailyRewardCycle.nextStreak(new DailyClaimState(5, TODAY.minusDays(2)), TODAY)).isEqualTo(1);
    }

    // --- cycleDay ---

    @Test
    void cycleDay_shouldBeZero_whenStreakIsZero() {
        assertThat(DailyRewardCycle.cycleDay(0, 7)).isEqualTo(0);
    }

    @Test
    void cycleDay_shouldBeZero_whenStreakIsNegative() {
        assertThat(DailyRewardCycle.cycleDay(-1, 7)).isEqualTo(0);
    }

    @Test
    void cycleDay_shouldMatchStreak_withinFirstCycle() {
        assertThat(DailyRewardCycle.cycleDay(3, 7)).isEqualTo(3);
    }

    @Test
    void cycleDay_shouldBeN_atLastDayOfCycle() {
        assertThat(DailyRewardCycle.cycleDay(7, 7)).isEqualTo(7);
    }

    @Test
    void cycleDay_shouldWrapToOne_afterCompletingCycle() {
        assertThat(DailyRewardCycle.cycleDay(8, 7)).isEqualTo(1);
    }

    @Test
    void cycleDay_shouldWrapCorrectly_acrossSecondCycle() {
        assertThat(DailyRewardCycle.cycleDay(14, 7)).isEqualTo(7);
        assertThat(DailyRewardCycle.cycleDay(15, 7)).isEqualTo(1);
    }

    // --- applyPlanBonus ---

    @Test
    void applyPlanBonus_shouldReturnBase_whenNoPlan() {
        assertThat(DailyRewardCycle.applyPlanBonus(10, false)).isEqualTo(10);
        assertThat(DailyRewardCycle.applyPlanBonus(15, false)).isEqualTo(15);
    }

    @Test
    void applyPlanBonus_shouldMultiplyByOnePointFive_whenHasPlan() {
        assertThat(DailyRewardCycle.applyPlanBonus(10, true)).isEqualTo(15);
        assertThat(DailyRewardCycle.applyPlanBonus(20, true)).isEqualTo(30);
    }

    @Test
    void applyPlanBonus_shouldRoundHalfUp_whenResultIsFractional() {
        // 15 * 1.5 = 22.5 -> 23
        assertThat(DailyRewardCycle.applyPlanBonus(15, true)).isEqualTo(23);
        // 25 * 1.5 = 37.5 -> 38
        assertThat(DailyRewardCycle.applyPlanBonus(25, true)).isEqualTo(38);
    }

    // --- currentStreak ---

    @Test
    void currentStreak_shouldBeStreak_whenAlive() {
        assertThat(DailyRewardCycle.currentStreak(new DailyClaimState(3, TODAY.minusDays(1)), TODAY)).isEqualTo(3);
    }

    @Test
    void currentStreak_shouldBeZero_whenStreakBroken() {
        assertThat(DailyRewardCycle.currentStreak(new DailyClaimState(3, TODAY.minusDays(2)), TODAY)).isEqualTo(0);
    }

    @Test
    void currentStreak_shouldBeZero_whenNeverClaimed() {
        assertThat(DailyRewardCycle.currentStreak(new DailyClaimState(0, null), TODAY)).isEqualTo(0);
    }

    // --- coinsForDay ---

    @Test
    void coinsForDay_shouldReturnCoinsOfThatDay_whenPresent() {
        assertThat(DailyRewardCycle.coinsForDay(sevenDayCycle(), 4)).isEqualTo(25);
        assertThat(DailyRewardCycle.coinsForDay(sevenDayCycle(), 7)).isEqualTo(100);
    }

    @Test
    void coinsForDay_shouldFallbackToFirstDay_whenDayMissingFromConfig() {
        List<DailyReward> gapped = List.of(
                DailyReward.builder().id(1L).dayNumber(1).coins(10).build(),
                DailyReward.builder().id(2L).dayNumber(5).coins(99).build());
        // El día 2 no existe en la config -> cae al primer día (10).
        assertThat(DailyRewardCycle.coinsForDay(gapped, 2)).isEqualTo(10);
    }

    // --- progress ---

    @Test
    void progress_shouldBeZeros_whenCycleEmpty() {
        CycleProgress p = DailyRewardCycle.progress(new DailyClaimState(3, TODAY.minusDays(1)), TODAY, 0, true);
        assertThat(p.nextDay()).isEqualTo(0);
        assertThat(p.completedDays()).isEqualTo(0);
    }

    @Test
    void progress_shouldReportNextDayAndCompleted_whenCanClaimConsecutive() {
        CycleProgress p = DailyRewardCycle.progress(new DailyClaimState(3, TODAY.minusDays(1)), TODAY, 7, true);
        assertThat(p.nextDay()).isEqualTo(4);
        assertThat(p.completedDays()).isEqualTo(3);
    }

    @Test
    void progress_shouldStartAtDayOne_whenCanClaimFirstTime() {
        CycleProgress p = DailyRewardCycle.progress(new DailyClaimState(0, null), TODAY, 7, true);
        assertThat(p.nextDay()).isEqualTo(1);
        assertThat(p.completedDays()).isEqualTo(0);
    }

    @Test
    void progress_shouldHaveNoNextDay_whenAlreadyClaimedToday() {
        CycleProgress p = DailyRewardCycle.progress(new DailyClaimState(3, TODAY), TODAY, 7, false);
        assertThat(p.nextDay()).isEqualTo(0);
        assertThat(p.completedDays()).isEqualTo(3);
    }

    @Test
    void progress_shouldUseCyclePosition_whenAlreadyClaimedAfterWrap() {
        CycleProgress p = DailyRewardCycle.progress(new DailyClaimState(8, TODAY), TODAY, 7, false);
        assertThat(p.nextDay()).isEqualTo(0);
        assertThat(p.completedDays()).isEqualTo(1); // cycleDay(8, 7) = 1
    }
}
