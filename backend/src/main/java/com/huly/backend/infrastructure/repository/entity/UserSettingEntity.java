package com.huly.backend.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_app_user")
    private AppUserEntity appUser;

    @Column(name = "music_volume")
    private Integer musicVolume;

    @Column(name = "effect_volume")
    private Integer effectVolume;

    @Column(name = "anti_scroll_enabled")
    private Boolean antiScrollEnabled;

    @Column(name = "darkmode")
    private Boolean darkmode;

}
