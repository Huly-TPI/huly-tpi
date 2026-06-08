package com.huly.backend.infrastructure.presentation.dto.user;

import com.huly.backend.domain.model.enums.ThemePreference;
import jakarta.validation.constraints.NotNull;

public record UpdateThemePreferenceRequest(
        @NotNull ThemePreference themePreference
) {
}
