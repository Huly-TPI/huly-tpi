package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.dto.user.GetUserCoinsRequest;
import com.huly.backend.domain.dto.user.GetUserCoinsResponse;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.AudioSettings;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.useCase.user.ChangePasswordUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import com.huly.backend.domain.useCase.user.GetUserCoinsUseCase;
import com.huly.backend.infrastructure.presentation.dto.user.ChangePasswordRequest;
import com.huly.backend.infrastructure.presentation.dto.user.UpdateAudioSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.user.UpdateThemePreferenceRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.user.UserPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GetCurrentUserUseCase getCurrentUserUseCase;
    private UserDetailDomainRepository userDetailDomainRepository;
    private GetUserCoinsUseCase getUserCoinsUseCase;
    private GetCurrentMembershipUseCase getCurrentMembershipUseCase;
    private ChangePasswordUseCase changePasswordUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getCurrentUserUseCase = mock(GetCurrentUserUseCase.class);
        userDetailDomainRepository = mock(UserDetailDomainRepository.class);
        getUserCoinsUseCase = mock(GetUserCoinsUseCase.class);
        getCurrentMembershipUseCase = mock(GetCurrentMembershipUseCase.class);
        changePasswordUseCase = mock(ChangePasswordUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        UserController userController = new UserController(
                getCurrentUserUseCase,
                userDetailDomainRepository,
                getUserCoinsUseCase,
                getCurrentMembershipUseCase,
                new UserPresentationMapper(),
                changePasswordUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void me_shouldReturnUserProfile_whenPrincipalIsValid() throws Exception {
        AppUser user = AppUser.builder()
                .id(USER_ID).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        UserProfile profile = new UserProfile(user, true, false, true);
        when(getCurrentUserUseCase.execute(USER_ID)).thenReturn(profile);
        when(userDetailDomainRepository.findThemePreference(USER_ID)).thenReturn(ThemePreference.DARK);
        when(userDetailDomainRepository.findAudioSettings(USER_ID))
                .thenReturn(new AudioSettings(0.25, 0.45, 0.85));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Mili"))
                .andExpect(jsonPath("$.email").value("user@huly.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.onBoardingCompleted").value(true))
                .andExpect(jsonPath("$.onboardingTutorialCompleted").value(false))
                .andExpect(jsonPath("$.profileOnboardingTutorialCompleted").value(true))
                .andExpect(jsonPath("$.themePreference").value("DARK"))
                .andExpect(jsonPath("$.audioSettings.interfaceVolume").value(0.25))
                .andExpect(jsonPath("$.audioSettings.ambientVolume").value(0.45))
                .andExpect(jsonPath("$.audioSettings.minigameVolume").value(0.85));
    }

    @Test
    void me_shouldThrowUnauthorized_whenPrincipalIsNull() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTheme_shouldPersistThemePreference_whenPrincipalIsValid() throws Exception {
        UpdateThemePreferenceRequest req = new UpdateThemePreferenceRequest(ThemePreference.DARK);

        mockMvc.perform(put("/api/users/me/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(userDetailDomainRepository).updateThemePreference(USER_ID, ThemePreference.DARK);
    }

    @Test
    void updateTheme_shouldThrowUnauthorized_whenPrincipalIsNull() throws Exception {
        SecurityContextHolder.clearContext();
        UpdateThemePreferenceRequest req = new UpdateThemePreferenceRequest(ThemePreference.DARK);

        mockMvc.perform(put("/api/users/me/theme")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAudioSettings_shouldPersistVolumes_whenPrincipalIsValid() throws Exception {
        UpdateAudioSettingsRequest req = new UpdateAudioSettingsRequest(0.2, 0.4, 0.6);
        when(userDetailDomainRepository.updateAudioSettings(USER_ID, new AudioSettings(0.2, 0.4, 0.6)))
                .thenReturn(new AudioSettings(0.2, 0.4, 0.6));

        mockMvc.perform(put("/api/users/me/audio-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interfaceVolume").value(0.2))
                .andExpect(jsonPath("$.ambientVolume").value(0.4))
                .andExpect(jsonPath("$.minigameVolume").value(0.6));

        verify(userDetailDomainRepository).updateAudioSettings(USER_ID, new AudioSettings(0.2, 0.4, 0.6));
    }

    @Test
    void updateAudioSettings_shouldRejectInvalidVolumes() throws Exception {
        UpdateAudioSettingsRequest req = new UpdateAudioSettingsRequest(1.2, 0.4, 0.6);

        mockMvc.perform(put("/api/users/me/audio-settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyCoins_shouldReturnCoins_whenPrincipalIsValid() throws Exception {
        when(getUserCoinsUseCase.execute(new GetUserCoinsRequest(USER_ID)))
                .thenReturn(new GetUserCoinsResponse(750));

        mockMvc.perform(get("/api/users/me/coins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(750));
    }

    @Test
    void getMyCoins_shouldReturnZero_whenUserHasNoCoins() throws Exception {
        when(getUserCoinsUseCase.execute(new GetUserCoinsRequest(USER_ID)))
                .thenReturn(new GetUserCoinsResponse(0));

        mockMvc.perform(get("/api/users/me/coins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(0));
    }

    @Test
    void getMyMembership_shouldReturnActiveMembership_whenUserHasOne() throws Exception {
        Instant expiresAt = Instant.parse("2026-07-01T00:00:00Z");
        when(getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(USER_ID)))
                .thenReturn(new GetCurrentMembershipResponse(true, "PREMIUM", 7L, expiresAt));

        mockMvc.perform(get("/api/users/me/membership"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.planCode").value("PREMIUM"))
                .andExpect(jsonPath("$.productId").value("7"))
                .andExpect(jsonPath("$.expiresAt").value("2026-07-01T00:00:00Z"));
    }

    @Test
    void getMyMembership_shouldReturnInactive_whenUserHasNoMembership() throws Exception {
        when(getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(USER_ID)))
                .thenReturn(GetCurrentMembershipResponse.inactive());

        mockMvc.perform(get("/api/users/me/membership"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.planCode").isEmpty())
                .andExpect(jsonPath("$.productId").isEmpty())
                .andExpect(jsonPath("$.expiresAt").isEmpty());
    }

    @Test
    void changePassword_shouldReturn204_whenRequestIsValid() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("currentPass", "newPass123");

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        verify(changePasswordUseCase).execute(USER_ID, "currentPass", "newPass123");
    }

    @Test
    void changePassword_shouldReturn401_whenCurrentPasswordIsWrong() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("wrongPass", "newPass123");
        doThrow(new InvalidCredentialsException("Current password is incorrect"))
                .when(changePasswordUseCase).execute(USER_ID, "wrongPass", "newPass123");

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldReturn400_whenCurrentPasswordIsBlank() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("", "newPass123");

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shouldReturn400_whenNewPasswordIsTooShort() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest("currentPass", "abc");

        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
