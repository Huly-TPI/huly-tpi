package com.huly.backend.presentation.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huly.backend.domain.model.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserRole role;
    private Boolean onBoardingCompleted;
}
