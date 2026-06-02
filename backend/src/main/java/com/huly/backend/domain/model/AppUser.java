package com.huly.backend.domain.model;

import java.time.LocalDate;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio del usuario. Representa la identidad del usuario sin dependencias de JPA.
 * Esta clase vive en el dominio y es independiente de la base de datos.
 *
 * Diferencia con AppUserEntity:
 *  - AppUser: objeto de dominio puro, usado en use cases y servicios de dominio
 *  - AppUserEntity: objeto de infraestructura, mapeado a la tabla app_user en BD
 *
 * Los repositorios del dominio (UserRepository) trabajan con AppUser.
 * Las implementaciones JPA de esos repositorios hacen la conversión a/desde AppUserEntity.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    private Long id;
    private String name;
    private String email;
    private String password; // Hash BCrypt (nunca la contraseña en plano)
    private LocalDate birthDate;
    private UserRole role;
    private UserStatus status;
}
