package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.badge.BadgeItem;
import com.huly.backend.domain.dto.badge.GetAllBadgesResponse;
import com.huly.backend.domain.dto.badge.GetUserBadgesRequest;
import com.huly.backend.domain.dto.badge.GetUserBadgesResponse;
import com.huly.backend.domain.dto.badge.UserBadgeItem;
import com.huly.backend.domain.useCase.badge.GetAllBadgesUseCase;
import com.huly.backend.domain.useCase.badge.GetUserBadgesUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.badge.BadgePresentationMapper;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BadgeControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private GetAllBadgesUseCase getAllBadgesUseCase;
    private GetUserBadgesUseCase getUserBadgesUseCase;

    @BeforeEach
    void setUp() {
        getAllBadgesUseCase = mock(GetAllBadgesUseCase.class);
        getUserBadgesUseCase = mock(GetUserBadgesUseCase.class);

        BadgeController badgeController = new BadgeController(
                getAllBadgesUseCase, getUserBadgesUseCase, new BadgePresentationMapper());

        mockMvc = MockMvcBuilders.standaloneSetup(badgeController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Devuelve la lista de insignias mapeada")
    void getAllBadgesShouldReturnMappedBadgeList() throws Exception {
        // --- arrange ---
        givenAllBadges(allBadges(namedBadge()));
        // --- act ---
        ResultActions result = performGetAllBadges();
        // --- assert ---
        thenOkWithBadgeList(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando no existen insignias")
    void getAllBadgesShouldReturnEmptyListWhenNoBadgesExist() throws Exception {
        // --- arrange ---
        givenAllBadges(allBadges());
        // --- act ---
        ResultActions result = performGetAllBadges();
        // --- assert ---
        thenOkWithEmptyArray(result);
    }

    @Test
    @DisplayName("Devuelve las insignias del usuario")
    void getMyBadgesShouldReturnUserBadges() throws Exception {
        // --- arrange ---
        givenUserBadges(userBadges(userBadge()));
        // --- act ---
        ResultActions result = performGetMyBadges();
        // --- assert ---
        thenOkWithUserBadges(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el usuario no tiene insignias")
    void getMyBadgesShouldReturnEmptyListWhenUserHasNoBadges() throws Exception {
        // --- arrange ---
        givenUserBadges(userBadges());
        // --- act ---
        ResultActions result = performGetMyBadges();
        // --- assert ---
        thenOkWithEmptyArray(result);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenAllBadges(GetAllBadgesResponse response) {
        when(getAllBadgesUseCase.execute()).thenReturn(response);
    }

    private void givenUserBadges(GetUserBadgesResponse response) {
        when(getUserBadgesUseCase.execute(any(GetUserBadgesRequest.class))).thenReturn(response);
    }

    private GetAllBadgesResponse allBadges(BadgeItem... badges) {
        return new GetAllBadgesResponse(List.of(badges));
    }

    private GetUserBadgesResponse userBadges(UserBadgeItem... badges) {
        return new GetUserBadgesResponse(List.of(badges));
    }

    private BadgeItem namedBadge() {
        return new BadgeItem(1L, "PRIMER PASO", "Primer paso", "Empezaste tu camino.", "badge_primer_paso.webp", null);
    }

    private UserBadgeItem userBadge() {
        BadgeItem badge = new BadgeItem(1L, "PRIMER PASO", "Primer paso", null, null, null);
        return new UserBadgeItem(1L, badge, Instant.parse("2026-01-01T00:00:00Z"));
    }

    // --- act ---
    private ResultActions performGetAllBadges() throws Exception {
        return mockMvc.perform(get("/api/badges"));
    }

    private ResultActions performGetMyBadges() throws Exception {
        return mockMvc.perform(get("/api/badges/my"));
    }

    // --- assert ---
    private void thenOkWithBadgeList(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("PRIMER PASO"))
                .andExpect(jsonPath("$[0].name").value("Primer paso"));
    }

    private void thenOkWithEmptyArray(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    private void thenOkWithUserBadges(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].badge.code").value("PRIMER PASO"))
                .andExpect(jsonPath("$[0].obtainedAt").value("2026-01-01T00:00:00Z"));
    }
}
