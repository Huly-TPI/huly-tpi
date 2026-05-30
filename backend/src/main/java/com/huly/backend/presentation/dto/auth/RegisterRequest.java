package com.huly.backend.presentation.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import com.huly.backend.infrastructure.validation.MinimumAge;

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
    @NotBlank
    private String lastname;

    @NotNull
    @Past
    @MinimumAge(13)
    private LocalDate birthDate;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}