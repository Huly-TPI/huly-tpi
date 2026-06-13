package com.huly.backend.domain.model.admin;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BackofficeUserSummary {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDate birthDate;
    private boolean antiScrollEnabled;
    private boolean dataSharingConsent;
    private String mostUsedApp;
    private Integer mostUsedAppActiveSeconds;
    private Integer totalScrollTimeSeconds;
}
