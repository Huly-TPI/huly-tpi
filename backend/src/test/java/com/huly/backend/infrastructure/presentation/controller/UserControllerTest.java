package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.user.GetCurrentMembershipRequest;
import com.huly.backend.domain.dto.user.GetCurrentMembershipResponse;
import com.huly.backend.domain.dto.user.GetUserCoinsRequest;
import com.huly.backend.domain.dto.user.GetUserCoinsResponse;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.user.AudioSettings;
import com.huly.backend.domain.model.user.UserAccountSettings;
import com.huly.backend.domain.model.user.UserProfile;
import com.huly.backend.domain.model.enums.ThemePreference;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.domain.repository.user.UserDetailDomainRepository;
import com.huly.backend.domain.useCase.auth.GetCurrentUserUseCase;
import com.huly.backend.domain.exception.InvalidCredentialsException;
import com.huly.backend.domain.useCase.user.ChangePasswordUseCase;
import com.huly.backend.domain.useCase.user.GetCurrentMembershipUseCase;
import com.huly.backend.domain.useCase.user.GetUserAccountSettingsUseCase;
import com.huly.backend.domain.useCase.user.GetUserCoinsUseCase;
import com.huly.backend.domain.useCase.user.UpdateUserAccountSettingsUseCase;
import com.huly.backend.infrastructure.presentation.dto.user.ChangePasswordRequest;
import com.huly.backend.infrastructure.presentation.dto.user.UpdateAudioSettingsRequest;
import com.huly.backend.infrastructure.presentation.dto.user.UpdateThemePreferenceRequest;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.user.UserPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private static final Long USER_ID = 1L;
    private static final Instant MEMBERSHIP_EXPIRES_AT = Instant.parse("2026-07-01T00:00:00Z");

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private GetCurrentUserUseCase getCurrentUserUseCase;
    private UserDetailDomainRepository userDetailDomainRepository;
    private GetUserCoinsUseCase getUserCoinsUseCase;
    private GetCurrentMembershipUseCase getCurrentMembershipUseCase;
    private ChangePasswordUseCase changePasswordUseCase;
    private GetUserAccountSettingsUseCase getUserAccountSettingsUseCase;
    private UpdateUserAccountSettingsUseCase updateUserAccountSettingsUseCase;

    @BeforeEach
    void setUp() {
        getCurrentUserUseCase = mock(GetCurrentUserUseCase.class);
        userDetailDomainRepository = mock(UserDetailDomainRepository.class);
        getUserCoinsUseCase = mock(GetUserCoinsUseCase.class);
        getCurrentMembershipUseCase = mock(GetCurrentMembershipUseCase.class);
        changePasswordUseCase = mock(ChangePasswordUseCase.class);
        getUserAccountSettingsUseCase = mock(GetUserAccountSettingsUseCase.class);
        updateUserAccountSettingsUseCase = mock(UpdateUserAccountSettingsUseCase.class);

        UserController userController = new UserController(
                getCurrentUserUseCase,
                userDetailDomainRepository,
                getUserCoinsUseCase,
                getCurrentMembershipUseCase,
                new UserPresentationMapper(),
                changePasswordUseCase,
                getUserAccountSettingsUseCase,
                updateUserAccountSettingsUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAsCurrentUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 200 con el perfil del usuario autenticado")
    void meShouldReturnUserProfileWhenPrincipalIsValid() throws Exception {
        givenCurrentUserProfile();

        ResultActions result = performGetMe();

        thenOkWithUserProfile(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la configuración de la cuenta del usuario autenticado")
    void getAccountSettingsShouldReturnSettingsForAuthenticatedUser() throws Exception {
        givenAccountSettings();

        ResultActions result = performGetAccountSettings();

        thenOkWithAccountSettingsAndVerified(result);
    }

    @Test
    @DisplayName("Actualiza y devuelve la configuración de la cuenta con los campos permitidos")
    void updateAccountSettingsShouldPersistAllowedFieldsForAuthenticatedUser() throws Exception {
        givenUpdatedAccountSettings();
        String body = accountSettingsBody("Mili", "2000-01-15");

        ResultActions result = performUpdateAccountSettings(body);

        thenOkWithUpdatedAccountSettings(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando el nombre está vacío")
    void updateAccountSettingsShouldRejectBlankName() throws Exception {
        String body = accountSettingsBody("", "2000-01-15");

        ResultActions result = performUpdateAccountSettings(body);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la fecha de nacimiento es futura")
    void updateAccountSettingsShouldRejectFutureBirthDate() throws Exception {
        String body = accountSettingsBody("Mili", "2999-01-15");

        ResultActions result = performUpdateAccountSettings(body);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 401 al obtener el perfil cuando no está autenticado")
    void meShouldThrowUnauthorizedWhenPrincipalIsNull() throws Exception {
        givenNoAuthentication();

        ResultActions result = performGetMe();

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Persiste la preferencia de tema y devuelve 204")
    void updateThemeShouldPersistThemePreferenceWhenPrincipalIsValid() throws Exception {
        String body = themeBody(ThemePreference.DARK);

        ResultActions result = performUpdateTheme(body);

        thenThemePersisted(result, ThemePreference.DARK);
    }

    @Test
    @DisplayName("Devuelve 401 al actualizar el tema cuando no está autenticado")
    void updateThemeShouldThrowUnauthorizedWhenPrincipalIsNull() throws Exception {
        givenNoAuthentication();
        String body = themeBody(ThemePreference.DARK);

        ResultActions result = performUpdateTheme(body);

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Persiste los volúmenes de audio y los devuelve")
    void updateAudioSettingsShouldPersistVolumesWhenPrincipalIsValid() throws Exception {
        givenAudioSettingsUpdated(0.2, 0.4, 0.6);
        String body = audioSettingsBody(0.2, 0.4, 0.6);

        ResultActions result = performUpdateAudioSettings(body);

        thenAudioSettingsReturned(result, 0.2, 0.4, 0.6);
    }

    @Test
    @DisplayName("Devuelve 400 cuando los volúmenes de audio son inválidos")
    void updateAudioSettingsShouldRejectInvalidVolumes() throws Exception {
        String body = audioSettingsBody(1.2, 0.4, 0.6);

        ResultActions result = performUpdateAudioSettings(body);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 200 con las monedas del usuario")
    void getMyCoinsShouldReturnCoinsWhenPrincipalIsValid() throws Exception {
        givenUserCoins(750);

        ResultActions result = performGetMyCoins();

        thenOkWithCoins(result, 750);
    }

    @Test
    @DisplayName("Devuelve 200 con cero monedas cuando el usuario no tiene monedas")
    void getMyCoinsShouldReturnZeroWhenUserHasNoCoins() throws Exception {
        givenUserCoins(0);

        ResultActions result = performGetMyCoins();

        thenOkWithCoins(result, 0);
    }

    @Test
    @DisplayName("Devuelve 200 con la membresía activa del usuario")
    void getMyMembershipShouldReturnActiveMembershipWhenUserHasOne() throws Exception {
        givenActiveMembership();

        ResultActions result = performGetMyMembership();

        thenOkWithActiveMembership(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la membresía inactiva cuando el usuario no tiene membresía")
    void getMyMembershipShouldReturnInactiveWhenUserHasNoMembership() throws Exception {
        givenNoMembership();

        ResultActions result = performGetMyMembership();

        thenOkWithInactiveMembership(result);
    }

    @Test
    @DisplayName("Devuelve 204 cuando la solicitud de cambio de contraseña es válida")
    void changePasswordShouldReturn204WhenRequestIsValid() throws Exception {
        String body = changePasswordBody("currentPass", "newPass123");

        ResultActions result = performChangePassword(body);

        thenPasswordChanged(result, "currentPass", "newPass123");
    }

    @Test
    @DisplayName("Devuelve 401 cuando la contraseña actual es incorrecta")
    void changePasswordShouldReturn401WhenCurrentPasswordIsWrong() throws Exception {
        givenChangePasswordFails("wrongPass", "newPass123");
        String body = changePasswordBody("wrongPass", "newPass123");

        ResultActions result = performChangePassword(body);

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la contraseña actual está vacía")
    void changePasswordShouldReturn400WhenCurrentPasswordIsBlank() throws Exception {
        String body = changePasswordBody("", "newPass123");

        ResultActions result = performChangePassword(body);

        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 400 cuando la nueva contraseña es demasiado corta")
    void changePasswordShouldReturn400WhenNewPasswordIsTooShort() throws Exception {
        String body = changePasswordBody("currentPass", "abc");

        ResultActions result = performChangePassword(body);

        thenBadRequest(result);
    }

    // --- arrange ---
    private void authenticateAsCurrentUser() {
        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenCurrentUserProfile() {
        AppUser user = AppUser.builder()
                .id(USER_ID).name("Mili").email("user@huly.com")
                .role(UserRole.USER).status(UserStatus.ACTIVE)
                .build();
        UserProfile profile = new UserProfile(user, true, false, true);
        when(getCurrentUserUseCase.execute(USER_ID)).thenReturn(profile);
        when(userDetailDomainRepository.findThemePreference(USER_ID)).thenReturn(ThemePreference.DARK);
        when(userDetailDomainRepository.findAudioSettings(USER_ID))
                .thenReturn(new AudioSettings(0.25, 0.45, 0.85));
        when(getUserAccountSettingsUseCase.execute(USER_ID))
                .thenReturn(new UserAccountSettings("Mili", "user@huly.com", LocalDate.of(2000, 1, 15)));
    }

    private void givenAccountSettings() {
        when(getUserAccountSettingsUseCase.execute(USER_ID))
                .thenReturn(new UserAccountSettings("Mili", "user@huly.com", LocalDate.of(2000, 1, 15)));
    }

    private void givenUpdatedAccountSettings() {
        when(updateUserAccountSettingsUseCase.execute(
                USER_ID,
                new UserAccountSettings("Mili", null, LocalDate.of(2000, 1, 15))
        )).thenReturn(new UserAccountSettings("Mili", "user@huly.com", LocalDate.of(2000, 1, 15)));
    }

    private void givenAudioSettingsUpdated(double interfaceVolume, double ambientVolume, double minigameVolume) {
        when(userDetailDomainRepository.updateAudioSettings(
                USER_ID, new AudioSettings(interfaceVolume, ambientVolume, minigameVolume)))
                .thenReturn(new AudioSettings(interfaceVolume, ambientVolume, minigameVolume));
    }

    private void givenUserCoins(int coins) {
        when(getUserCoinsUseCase.execute(new GetUserCoinsRequest(USER_ID)))
                .thenReturn(new GetUserCoinsResponse(coins));
    }

    private void givenActiveMembership() {
        when(getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(USER_ID)))
                .thenReturn(new GetCurrentMembershipResponse(true, "PREMIUM", 7L, MEMBERSHIP_EXPIRES_AT));
    }

    private void givenNoMembership() {
        when(getCurrentMembershipUseCase.execute(new GetCurrentMembershipRequest(USER_ID)))
                .thenReturn(GetCurrentMembershipResponse.inactive());
    }

    private void givenChangePasswordFails(String currentPassword, String newPassword) {
        doThrow(new InvalidCredentialsException("Current password is incorrect"))
                .when(changePasswordUseCase).execute(USER_ID, currentPassword, newPassword);
    }

    private String accountSettingsBody(String name, String birthDate) {
        return """
                {
                  "name": "%s",
                  "birthDate": "%s"
                }
                """.formatted(name, birthDate);
    }

    private String themeBody(ThemePreference themePreference) throws Exception {
        return objectMapper.writeValueAsString(new UpdateThemePreferenceRequest(themePreference));
    }

    private String audioSettingsBody(double interfaceVolume, double ambientVolume, double minigameVolume)
            throws Exception {
        return objectMapper.writeValueAsString(
                new UpdateAudioSettingsRequest(interfaceVolume, ambientVolume, minigameVolume));
    }

    private String changePasswordBody(String currentPassword, String newPassword) throws Exception {
        return objectMapper.writeValueAsString(new ChangePasswordRequest(currentPassword, newPassword));
    }

    // --- act ---
    private ResultActions performGetMe() throws Exception {
        return mockMvc.perform(get("/api/users/me"));
    }

    private ResultActions performGetAccountSettings() throws Exception {
        return mockMvc.perform(get("/api/users/me/settings"));
    }

    private ResultActions performUpdateAccountSettings(String body) throws Exception {
        return mockMvc.perform(put("/api/users/me/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions performUpdateTheme(String body) throws Exception {
        return mockMvc.perform(put("/api/users/me/theme")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions performUpdateAudioSettings(String body) throws Exception {
        return mockMvc.perform(put("/api/users/me/audio-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions performGetMyCoins() throws Exception {
        return mockMvc.perform(get("/api/users/me/coins"));
    }

    private ResultActions performGetMyMembership() throws Exception {
        return mockMvc.perform(get("/api/users/me/membership"));
    }

    private ResultActions performChangePassword(String body) throws Exception {
        return mockMvc.perform(put("/api/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    // --- assert ---
    private void thenOkWithUserProfile(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Mili"))
                .andExpect(jsonPath("$.email").value("user@huly.com"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-15"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.onBoardingCompleted").value(true))
                .andExpect(jsonPath("$.onboardingTutorialCompleted").value(false))
                .andExpect(jsonPath("$.profileOnboardingTutorialCompleted").value(true))
                .andExpect(jsonPath("$.themePreference").value("DARK"))
                .andExpect(jsonPath("$.audioSettings.interfaceVolume").value(0.25))
                .andExpect(jsonPath("$.audioSettings.ambientVolume").value(0.45))
                .andExpect(jsonPath("$.audioSettings.minigameVolume").value(0.85));
    }

    private void thenOkWithAccountSettingsAndVerified(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mili"))
                .andExpect(jsonPath("$.email").value("user@huly.com"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-15"));
        verify(getUserAccountSettingsUseCase).execute(USER_ID);
    }

    private void thenOkWithUpdatedAccountSettings(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mili"))
                .andExpect(jsonPath("$.email").value("user@huly.com"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-15"));
    }

    private void thenThemePersisted(ResultActions result, ThemePreference themePreference) throws Exception {
        result.andExpect(status().isNoContent());
        verify(userDetailDomainRepository).updateThemePreference(USER_ID, themePreference);
    }

    private void thenAudioSettingsReturned(ResultActions result,
                                           double interfaceVolume, double ambientVolume, double minigameVolume)
            throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.interfaceVolume").value(interfaceVolume))
                .andExpect(jsonPath("$.ambientVolume").value(ambientVolume))
                .andExpect(jsonPath("$.minigameVolume").value(minigameVolume));
        verify(userDetailDomainRepository).updateAudioSettings(
                USER_ID, new AudioSettings(interfaceVolume, ambientVolume, minigameVolume));
    }

    private void thenOkWithCoins(ResultActions result, int coins) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(coins));
    }

    private void thenOkWithActiveMembership(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.planCode").value("PREMIUM"))
                .andExpect(jsonPath("$.productId").value("7"))
                .andExpect(jsonPath("$.expiresAt").value("2026-07-01T00:00:00Z"));
    }

    private void thenOkWithInactiveMembership(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.planCode").isEmpty())
                .andExpect(jsonPath("$.productId").isEmpty())
                .andExpect(jsonPath("$.expiresAt").isEmpty());
    }

    private void thenPasswordChanged(ResultActions result, String currentPassword, String newPassword)
            throws Exception {
        result.andExpect(status().isNoContent());
        verify(changePasswordUseCase).execute(USER_ID, currentPassword, newPassword);
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}
