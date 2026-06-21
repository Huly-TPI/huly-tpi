package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardRequest;
import com.huly.backend.domain.dto.comebackReward.ClaimComebackRewardResponse;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusRequest;
import com.huly.backend.domain.dto.comebackReward.GetComebackRewardStatusResponse;
import com.huly.backend.domain.useCase.comebackReward.ClaimComebackRewardUseCase;
import com.huly.backend.domain.useCase.comebackReward.GetComebackRewardStatusUseCase;
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

        ComebackRewardController controller = new ComebackRewardController(getComebackRewardStatusUseCase, claimComebackRewardUseCase);
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
    void getStatus_shouldReturn200WithStatus() throws Exception {
        when(getComebackRewardStatusUseCase.execute(new GetComebackRewardStatusRequest(USER_ID)))
                .thenReturn(new GetComebackRewardStatusResponse(true, 10, 50, 10));

        mockMvc.perform(get("/api/comeback-rewards/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.daysInactive").value(10))
                .andExpect(jsonPath("$.coins").value(50))
                .andExpect(jsonPath("$.thresholdDays").value(10));
    }

    @Test
    void claim_shouldReturn200WithReward() throws Exception {
        when(claimComebackRewardUseCase.execute(new ClaimComebackRewardRequest(USER_ID)))
                .thenReturn(new ClaimComebackRewardResponse(true, 50, 10));

        mockMvc.perform(post("/api/comeback-rewards/claim"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granted").value(true))
                .andExpect(jsonPath("$.coins").value(50))
                .andExpect(jsonPath("$.daysInactive").value(10));
    }

    @Test
    void claim_shouldReturn401_whenNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/comeback-rewards/claim"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStatus_shouldReturn401_whenPrincipalUsernameNotNumeric() throws Exception {
        authenticateAs("not-a-number");

        mockMvc.perform(get("/api/comeback-rewards/status"))
                .andExpect(status().isUnauthorized());
    }
}
