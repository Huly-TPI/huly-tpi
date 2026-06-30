package com.huly.backend.domain.model.dailyReward;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DailyClaimStateTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 12);

    @Test
    void claimedOn_shouldBeTrue_whenLastClaimIsThatDay() {
        assertThat(new DailyClaimState(3, TODAY).claimedOn(TODAY)).isTrue();
    }

    @Test
    void claimedOn_shouldBeFalse_whenLastClaimIsAnotherDay() {
        assertThat(new DailyClaimState(3, TODAY.minusDays(1)).claimedOn(TODAY)).isFalse();
    }

    @Test
    void claimedOn_shouldBeFalse_whenNeverClaimed() {
        assertThat(new DailyClaimState(0, null).claimedOn(TODAY)).isFalse();
    }
}
