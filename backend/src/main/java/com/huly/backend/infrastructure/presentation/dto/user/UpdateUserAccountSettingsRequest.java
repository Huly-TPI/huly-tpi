package com.huly.backend.infrastructure.presentation.dto.user;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateUserAccountSettingsRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String name,

        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate birthDate
) {}
