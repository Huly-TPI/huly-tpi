package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardRequest;
import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.useCase.dailyReward.ClaimDailyRewardUseCase;
import com.huly.backend.domain.useCase.dailyReward.GetDailyRewardStatusUseCase;
import com.huly.backend.infrastructure.presentation.mapper.dailyReward.DailyRewardPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyRewardControllerTest {

    private static final Long USER_ID = 10L;

    private MockMvc mockMvc;
    private ClaimDailyRewardUseCase claimDailyRewardUseCase;
    private GetDailyRewardStatusUseCase getDailyRewardStatusUseCase;

    @BeforeEach
    void setUp() {
        claimDailyRewardUseCase = mock(ClaimDailyRewardUseCase.class);
        getDailyRewardStatusUseCase = mock(GetDailyRewardStatusUseCase.class);
        DailyRewardController controller = new DailyRewardController(
                claimDailyRewardUseCase, getDailyRewardStatusUseCase, new DailyRewardPresentationMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve 200 con el calendario de recompensas diarias")
    void getStatusShouldReturn200WithCalendar() throws Exception {
        givenStatus(statusResponse(false));

        ResultActions result = performGetStatus();

        thenOkWithBaseCalendar(result);
    }

    @Test
    @DisplayName("Devuelve las monedas potenciadas cuando el bono de plan está activo")
    void getStatusShouldReturnMultipliedCoinsWhenPlanBonusActive() throws Exception {
        givenStatus(statusResponse(true));

        ResultActions result = performGetStatus();

        thenOkWithMultipliedCoins(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la recompensa reclamada")
    void claimShouldReturn200WithReward() throws Exception {
        givenClaim(new ClaimDailyRewardResponse(10, 1, 1));

        ResultActions result = performClaim();

        thenOkWithReward(result, 10, 1, 1);
    }

    @Test
    @DisplayName("Devuelve 401 al reclamar cuando no está autenticado")
    void claimShouldReturn401WhenNotAuthenticated() throws Exception {
        givenNoAuthentication();

        ResultActions result = performClaim();

        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 401 cuando el usuario del principal no es numérico")
    void getStatusShouldReturn401WhenPrincipalUsernameNotNumeric() throws Exception {
        authenticateAs("not-a-number");

        ResultActions result = performGetStatus();

        thenUnauthorized(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenNoAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void givenStatus(GetDailyRewardStatusResponse status) {
        when(getDailyRewardStatusUseCase.execute(new GetDailyRewardStatusRequest(USER_ID))).thenReturn(status);
    }

    private void givenClaim(ClaimDailyRewardResponse response) {
        when(claimDailyRewardUseCase.execute(new ClaimDailyRewardRequest(USER_ID))).thenReturn(response);
    }

    private GetDailyRewardStatusResponse statusResponse(boolean planBonusActive) {
        return new GetDailyRewardStatusResponse(
                List.of(
                        DailyReward.builder().id(1L).dayNumber(1).coins(10).build(),
                        DailyReward.builder().id(2L).dayNumber(2).coins(15).build()
                ),
                3, 3, false, 0, planBonusActive);
    }

    // --- act ---
    private ResultActions performGetStatus() throws Exception {
        return mockMvc.perform(get("/api/daily-rewards/status"));
    }

    private ResultActions performClaim() throws Exception {
        return mockMvc.perform(post("/api/daily-rewards/claim"));
    }

    // --- assert ---
    private void thenOkWithBaseCalendar(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].dayNumber").value(1))
                .andExpect(jsonPath("$.days[0].coins").value(10))
                .andExpect(jsonPath("$.days[1].dayNumber").value(2))
                .andExpect(jsonPath("$.days[1].coins").value(15))
                .andExpect(jsonPath("$.currentStreak").value(3))
                .andExpect(jsonPath("$.completedDays").value(3))
                .andExpect(jsonPath("$.canClaimToday").value(false))
                .andExpect(jsonPath("$.nextDay").value(0))
                .andExpect(jsonPath("$.planBonusActive").value(false));
    }

    private void thenOkWithMultipliedCoins(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                // Monedas ya potenciadas x1.5: 10 -> 15, 15 -> 23 (round(22.5)).
                .andExpect(jsonPath("$.days[0].coins").value(15))
                .andExpect(jsonPath("$.days[1].coins").value(23))
                .andExpect(jsonPath("$.planBonusActive").value(true));
    }

    private void thenOkWithReward(ResultActions result, int coins, int dayNumber, int newStreak) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(coins))
                .andExpect(jsonPath("$.dayNumber").value(dayNumber))
                .andExpect(jsonPath("$.newStreak").value(newStreak));
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}
