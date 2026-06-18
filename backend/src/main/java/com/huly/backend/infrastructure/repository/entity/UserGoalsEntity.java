package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.GoalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoalsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user")
    private AppUserEntity appUser;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private GoalStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = true)
    private ActivityEntity activity;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "coins_reward", nullable = false)
    private Integer coinsReward = 10;

    @Column(name = "coins_reward_with_image", nullable = false)
    private Integer coinsRewardWithImage = 25;

}
