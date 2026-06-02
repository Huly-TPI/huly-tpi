package com.huly.backend.infrastructure.repository.entity;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entidad JPA que representa la tabla "app_user" en la base de datos.
 * Contiene solo los datos de autenticación/autorización del usuario.
 * Los datos de perfil (nombre, fecha de nacimiento) están en UserDetailEntity.
 *
 * Relaciones:
 *  - userDetails: datos de perfil extendidos (1:N por flexibilidad histórica, en práctica es 1:1)
 *  - refreshTokens: tokens JWT activos del usuario (múltiples sesiones simultáneas posibles)
 *  - userSettings: preferencias y configuraciones del usuario
 *  - userGoals: objetivos/metas del usuario
 *  - breathingSessions: historial de sesiones de respiración
 *  - journals: diarios emocionales
 *  - chatSessions: sesiones de conversación con el chatbot
 *
 * cascade=ALL + orphanRemoval=true: al eliminar un usuario, se eliminan en cascada todos sus datos.
 */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "password")
    private String password; // Siempre almacenado como hash BCrypt, nunca en plano

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING) // Guarda el nombre del enum como string en BD (no el ordinal)
    @Column(name = "role", length = 50)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private UserStatus status;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserDetailEntity> userDetails;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshTokenEntity> refreshTokens;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSettingEntity> userSettings;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGoalsEntity> userGoals;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BreathingSessionsEntity> breathingSessions;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntity> journals;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatSessionEntity> chatSessions;
}
