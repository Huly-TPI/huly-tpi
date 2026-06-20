package com.huly.backend.domain.model.user;

import java.time.LocalDate;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    private Long id;
    private String name;
    private String email;
    private String password;
    private LocalDate birthDate;
    private UserRole role;
    private UserStatus status;
}
