package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.SourceAction;
import com.huly.backend.domain.model.enums.ThemePreference;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user")
    private AppUserEntity appUser;

    @Column(name = "name")
    private String name;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "on_boarding_completed")
    private Boolean onBoardingCompleted;

    @Column(name = "profile_on_boarding_completed")
    private Boolean profileOnBoardingCompleted;

    @Column(name = "onboarding_tutorial_completed", nullable = false)
    private Boolean onboardingTutorialCompleted;

    @Column(name = "profile_onboarding_tutorial_completed", nullable = false)
    private Boolean profileOnboardingTutorialCompleted;

    @Column(name = "avatar_url_2")
    private String avatarUrl2;

    @Column(name = "last_login_date")
    private Instant lastLoginDate;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_action", length = 50)
    private SourceAction sourceAction;

    @Column(name = "onboarding_answer_1")
    private String onboardingAnswer1;

    @Column(name = "onboarding_answer_2")
    private String onboardingAnswer2;

    @Column(name = "onboarding_answer_3")
    private String onboardingAnswer3;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme_preference", nullable = false, length = 20)
    private ThemePreference themePreference;

    @Builder.Default
    @Column(name = "daily_reward_streak", nullable = false)
    private Integer dailyRewardStreak = 0;

    @Column(name = "last_daily_claim_date")
    private LocalDate lastDailyClaimDate;

    @Column(name = "inactivity_reminder_sent_at")
    private Instant inactivityReminderSentAt;

}
