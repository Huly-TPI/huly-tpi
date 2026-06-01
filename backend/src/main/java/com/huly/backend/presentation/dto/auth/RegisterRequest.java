package com.huly.backend.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import com.huly.backend.infrastructure.validation.MinimumAge;

@Getter
@Setter
public class RegisterRequest {

    @NotNull
    @Email
    @NotBlank
    private String email;

    @NotNull
    @NotBlank
    @Size(min = 6)
    private String password;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    @Past
    @MinimumAge(13)
    private LocalDate birthDate;
}