package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "user_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user")
    private AppUser appUser;

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
    private OffsetDateTime lastLoginDate;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

}
