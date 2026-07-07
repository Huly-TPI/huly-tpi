package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardRequest;
import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.domain.useCase.comebackReward.ClaimComebackRewardUseCase;
import com.huly.backend.domain.useCase.comebackReward.GetComebackRewardStatusUseCase;
import com.huly.backend.infrastructure.presentation.mapper.comebackReward.ComebackRewardPresentationMapper;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ComebackRewardControllerTest {

    private static final Long USER_ID = 10L;

    private MockMvc mockMvc;
    private GetComebackRewardStatusUseCase getComebackRewardStatusUseCase;
    private ClaimComebackRewardUseCase claimComebackRewardUseCase;

    @BeforeEach
    void setUp() {
        getComebackRewardStatusUseCase = mock(GetComebackRewardStatusUseCase.class);
        claimComebackRewardUseCase = mock(ClaimComebackRewardUseCase.class);

        ComebackRewardController controller = new ComebackRewardController(
                getComebackRewardStatusUseCase, claimComebackRewardUseCase, new ComebackRewardPresentationMapper());
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
    @DisplayName("Devuelve 200 con el estado de la recompensa de regreso")
    void getStatusShouldReturn200WithStatus() throws Exception {
        // --- arrange ---
        givenStatus(new GetComebackRewardStatusResponse(true, 10, 50, 10));
        // --- act ---
        ResultActions result = performGetStatus();
        // --- assert ---
        thenOkWithStatus(result);
    }

    @Test
    @DisplayName("Devuelve 200 con la recompensa reclamada")
    void claimShouldReturn200WithReward() throws Exception {
        // --- arrange ---
        givenClaim(new ClaimComebackRewardResponse(true, 50, 10));
        // --- act ---
        ResultActions result = performClaim();
        // --- assert ---
        thenOkWithReward(result);
    }

    @Test
    @DisplayName("Devuelve 401 al reclamar cuando no está autenticado")
    void claimShouldReturn401WhenNotAuthenticated() throws Exception {
        // --- arrange ---
        givenNoAuthentication();
        // --- act ---
        ResultActions result = performClaim();
        // --- assert ---
        thenUnauthorized(result);
    }

    @Test
    @DisplayName("Devuelve 401 cuando el usuario del principal no es numérico")
    void getStatusShouldReturn401WhenPrincipalUsernameNotNumeric() throws Exception {
        // --- arrange ---
        authenticateAs("not-a-number");
        // --- act ---
        ResultActions result = performGetStatus();
        // --- assert ---
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

    private void givenStatus(GetComebackRewardStatusResponse response) {
        when(getComebackRewardStatusUseCase.execute(new GetComebackRewardStatusRequest(USER_ID))).thenReturn(response);
    }

    private void givenClaim(ClaimComebackRewardResponse response) {
        when(claimComebackRewardUseCase.execute(new ClaimComebackRewardRequest(USER_ID))).thenReturn(response);
    }

    // --- act ---
    private ResultActions performGetStatus() throws Exception {
        return mockMvc.perform(get("/api/comeback-rewards/status"));
    }

    private ResultActions performClaim() throws Exception {
        return mockMvc.perform(post("/api/comeback-rewards/claim"));
    }

    // --- assert ---
    private void thenOkWithStatus(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.daysInactive").value(10))
                .andExpect(jsonPath("$.coins").value(50))
                .andExpect(jsonPath("$.thresholdDays").value(10));
    }

    private void thenOkWithReward(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true))
                .andExpect(jsonPath("$.coins").value(50))
                .andExpect(jsonPath("$.daysInactive").value(10));
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}
