package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_reward")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRewardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "day_number", nullable = false, unique = true)
    private Integer dayNumber;

    @Column(name = "coins", nullable = false)
    private Integer coins;
}
