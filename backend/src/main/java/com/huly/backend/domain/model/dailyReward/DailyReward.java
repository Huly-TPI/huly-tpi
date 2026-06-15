package com.huly.backend.domain.model.dailyReward;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class DailyReward {

    private final Long id;
    private final int dayNumber;
    private final int coins;
}
