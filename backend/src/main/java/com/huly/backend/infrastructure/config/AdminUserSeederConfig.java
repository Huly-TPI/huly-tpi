package com.huly.backend.infrastructure.config;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeder del usuario administrador para el entorno de desarrollo.
 * Solo se ejecuta con el perfil "dev" (@Profile("dev")), nunca en producción.
 * CommandLineRunner se ejecuta automáticamente al iniciar la aplicación.
 *
 * ADVERTENCIA: las credenciales admin123 son solo para desarrollo local.
 * En producción, los usuarios admin se crean por otro mecanismo seguro.
 */
@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class AdminUserSeederConfig {

    @Bean
    CommandLineRunner createAdminUser(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            String adminEmail = "admin@huly.com";

            // Idempotente: no crea el admin si ya existe (evita duplicados en reinicios)
            boolean exists = appUserRepository.existsByEmail(adminEmail);

            if (exists) {
                System.out.println("Admin user ya existente");
                return;
            }

            AppUserEntity admin = AppUserEntity.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123")) // Solo dev, nunca hardcodear en producción
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            appUserRepository.save(admin);

            System.out.println("Admin user creado: " + adminEmail);
        };
    }
}
