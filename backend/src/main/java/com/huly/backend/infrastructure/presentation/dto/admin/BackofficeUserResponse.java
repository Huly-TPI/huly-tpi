package com.huly.backend.infrastructure.presentation.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BackofficeUserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String status;
    private LocalDate birthDate;
    private boolean antiScrollEnabled;
    private boolean dataSharingConsent;
    private String mostUsedApp;
    private Integer mostUsedAppActiveSeconds;
    private Integer totalScrollTimeSeconds;
}
