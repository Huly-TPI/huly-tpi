package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.Badge;
import com.huly.backend.domain.model.UserBadge;
import com.huly.backend.domain.useCase.badge.GetAllBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BadgeControllerTest {

    private MockMvc mockMvc;
    private GetAllBadgesUseCase getAllBadgesUseCase;
    private GetUserBadgesUseCase getUserBadgesUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        getAllBadgesUseCase = mock(GetAllBadgesUseCase.class);
        getUserBadgesUseCase = mock(GetUserBadgesUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        BadgeController badgeController = new BadgeController(getAllBadgesUseCase, getUserBadgesUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(badgeController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllBadges_shouldReturnMappedBadgeList() throws Exception {
        Badge badge = Badge.builder()
                .id(1L)
                .code("PRIMER PASO")
                .name("Primer paso")
                .description("Empezaste tu camino.")
                .imageUrl("badge_primer_paso.webp")
                .build();
        when(getAllBadgesUseCase.execute()).thenReturn(List.of(badge));

        mockMvc.perform(get("/api/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("PRIMER PASO"))
                .andExpect(jsonPath("$[0].name").value("Primer paso"));
    }

    @Test
    void getAllBadges_shouldReturnEmptyList_whenNoBadgesExist() throws Exception {
        when(getAllBadgesUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/api/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyBadges_shouldReturnUserBadges() throws Exception {
        Badge badge = Badge.builder()
                .id(1L)
                .code("PRIMER PASO")
                .name("Primer paso")
                .build();
        UserBadge userBadge = UserBadge.builder()
                .id(1L)
                .userId(USER_ID)
                .badge(badge)
                .obtainedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
        when(getUserBadgesUseCase.execute(USER_ID)).thenReturn(List.of(userBadge));

        mockMvc.perform(get("/api/badges/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].badge.code").value("PRIMER PASO"))
                .andExpect(jsonPath("$[0].obtainedAt").value("2026-01-01T00:00:00Z"));
    }

    @Test
    void getMyBadges_shouldReturnEmptyList_whenUserHasNoBadges() throws Exception {
        when(getUserBadgesUseCase.execute(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/badges/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
