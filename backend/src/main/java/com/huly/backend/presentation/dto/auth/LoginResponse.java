package com.huly.backend.presentation.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;

    private String refreshToken;
    
}