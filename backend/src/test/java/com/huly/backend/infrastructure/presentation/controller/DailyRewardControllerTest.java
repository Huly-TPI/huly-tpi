package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardRequest;
import com.huly.backend.domain.dto.dailyReward.ClaimDailyRewardResponse;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusRequest;
import com.huly.backend.domain.dto.dailyReward.GetDailyRewardStatusResponse;
import com.huly.backend.domain.model.dailyReward.DailyReward;
import com.huly.backend.domain.useCase.dailyReward.ClaimDailyRewardUseCase;
import com.huly.backend.domain.useCase.dailyReward.GetDailyRewardStatusUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
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

        DailyRewardController controller = new DailyRewardController(claimDailyRewardUseCase, getDailyRewardStatusUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    @Test
    void getStatus_shouldReturn200WithCalendar() throws Exception {
        GetDailyRewardStatusResponse status = new GetDailyRewardStatusResponse(
                List.of(
                        DailyReward.builder().id(1L).dayNumber(1).coins(10).build(),
                        DailyReward.builder().id(2L).dayNumber(2).coins(15).build()
                ),
                3, 3, false, 0, false);
        when(getDailyRewardStatusUseCase.execute(new GetDailyRewardStatusRequest(USER_ID))).thenReturn(status);

        mockMvc.perform(get("/api/daily-rewards/status"))
                .andExpect(status().isOk())
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

    @Test
    void getStatus_shouldReturnMultipliedCoins_whenPlanBonusActive() throws Exception {
        GetDailyRewardStatusResponse status = new GetDailyRewardStatusResponse(
                List.of(
                        DailyReward.builder().id(1L).dayNumber(1).coins(10).build(),
                        DailyReward.builder().id(2L).dayNumber(2).coins(15).build()
                ),
                3, 3, false, 0, true);
        when(getDailyRewardStatusUseCase.execute(new GetDailyRewardStatusRequest(USER_ID))).thenReturn(status);

        mockMvc.perform(get("/api/daily-rewards/status"))
                .andExpect(status().isOk())
                // Coins ya potenciados x1.5: 10 -> 15, 15 -> 23 (round(22.5)).
                .andExpect(jsonPath("$.days[0].coins").value(15))
                .andExpect(jsonPath("$.days[1].coins").value(23))
                .andExpect(jsonPath("$.planBonusActive").value(true));
    }

    @Test
    void claim_shouldReturn200WithReward() throws Exception {
        when(claimDailyRewardUseCase.execute(new ClaimDailyRewardRequest(USER_ID))).thenReturn(new ClaimDailyRewardResponse(10, 1, 1));

        mockMvc.perform(post("/api/daily-rewards/claim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(10))
                .andExpect(jsonPath("$.dayNumber").value(1))
                .andExpect(jsonPath("$.newStreak").value(1));
    }

    @Test
    void claim_shouldReturn401_whenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/daily-rewards/claim"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStatus_shouldReturn401_whenPrincipalUsernameNotNumeric() throws Exception {
        authenticateAs("not-a-number");

        mockMvc.perform(get("/api/daily-rewards/status"))
                .andExpect(status().isUnauthorized());
    }
}
