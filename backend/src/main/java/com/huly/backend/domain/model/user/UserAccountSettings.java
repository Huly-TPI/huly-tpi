package com.huly.backend.domain.model.user;

import java.time.LocalDate;

public record UserAccountSettings(
        String name,
        String email,
        LocalDate birthDate
) {}
