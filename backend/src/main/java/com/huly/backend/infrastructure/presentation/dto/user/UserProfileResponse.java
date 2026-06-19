package com.huly.backend.infrastructure.presentation.dto.user;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean onBoardingCompleted;
    private Boolean onboardingTutorialCompleted;
    private Boolean profileOnboardingTutorialCompleted;
    private ThemePreference themePreference;
}
