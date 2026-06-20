package com.huly.backend.domain.model.auth;

import com.huly.backend.domain.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokens {
    private String accessToken;
    private String refreshToken;
    private UserRole role;
    private Boolean onBoardingCompleted;
}
