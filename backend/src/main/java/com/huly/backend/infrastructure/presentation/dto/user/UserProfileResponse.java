package com.huly.backend.infrastructure.presentation.dto.user;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private UserRole role;
    private Boolean onBoardingCompleted;
    private Boolean onboardingTutorialCompleted;
    private Boolean profileOnboardingTutorialCompleted;
    private ThemePreference themePreference;
    private AudioSettingsResponse audioSettings;
}
