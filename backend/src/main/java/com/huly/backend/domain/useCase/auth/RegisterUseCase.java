package com.huly.backend.domain.useCase.auth;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.AuthTokens;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.provider.PasswordHasher;
import com.huly.backend.domain.repository.UserRepository;
import com.huly.backend.exception.ConflictException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: registrar un nuevo usuario en el sistema.
 * Verifica unicidad de email, hashea la contraseña y persiste el usuario con rol USER.
 * Al finalizar, llama a LoginUseCase para hacer login automático y devolver los tokens,
 * evitando que el usuario tenga que autenticarse por separado después del registro.
 */
@Service
@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final LoginUseCase loginUseCase;

    @Transactional
    public AuthTokens execute(String email, String rawPassword, String name, LocalDate birthDate) {
        // Verifica que el email no esté ya registrado (unicidad)
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use");
        }

        // Crea el usuario con la contraseña hasheada (nunca se guarda en plano)
        userRepository.save(AppUser.builder()
                .name(name)
                .email(email)
                .password(passwordHasher.encode(rawPassword))
                .birthDate(birthDate)
                .role(UserRole.USER)       // Todo usuario nuevo es USER por defecto
                .status(UserStatus.ACTIVE) // Activo desde el primer momento (sin verificación de email)
                .build());

        // Login automático: reutiliza LoginUseCase para no duplicar la lógica de generación de tokens
        return loginUseCase.execute(email, rawPassword);
    }
}
