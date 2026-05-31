package com.huly.backend.domain.service;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.exception.ConflictException;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.security.JwtService;
import com.huly.backend.presentation.dto.auth.LoginRequest;
import com.huly.backend.presentation.dto.auth.LoginResponse;
import com.huly.backend.presentation.dto.auth.RegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final UserDetailRepository userDetailRepository;

    public LoginResponse login(LoginRequest request) {

        AppUserEntity user = appUserRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials")
                );

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken =
                jwtService.generateAccessToken(user.getEmail());

        String refreshToken =
                jwtService.generateRefreshToken(user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    @Transactional
    public LoginResponse register(RegisterRequest request){

        boolean userExists = appUserRepository
                .existsByEmail(request.getEmail());

        if (userExists) {
                throw new ConflictException("El email ya esta registrado");
        }

        AppUserEntity user = new AppUserEntity();

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        AppUserEntity savedUser = appUserRepository.save(user);

        UserDetailEntity userDetail = UserDetailEntity.builder()
                .appUser(savedUser)
                .name(request.getName())
                .birth(request.getBirthDate())
                .build();

        userDetailRepository.save(userDetail);

        String accessToken =
                jwtService.generateAccessToken(savedUser.getEmail());
        
        String refreshToken =
                jwtService.generateRefreshToken(savedUser.getEmail());


        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}