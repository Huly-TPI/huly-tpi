package com.huly.backend.infrastructure.repository.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.huly.backend.domain.model.enums.SourceAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

}