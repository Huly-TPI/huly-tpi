package com.huly.backend.infrastructure.presentation.dto.user;

import java.time.LocalDate;

public record UserAccountSettingsResponse(
        String name,
        String email,
        LocalDate birthDate
) {}
