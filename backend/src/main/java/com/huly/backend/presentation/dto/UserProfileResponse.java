package com.huly.backend.presentation.dto;
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
}

