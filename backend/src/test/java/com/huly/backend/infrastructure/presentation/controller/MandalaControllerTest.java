package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MandalaControllerTest {

    private static final Long USER_ID = 7L;

    private MockMvc mockMvc;
    private ListAvailableMandalasUseCase listAvailableMandalasUseCase;

    @BeforeEach
    void setUp() {
        listAvailableMandalasUseCase = mock(ListAvailableMandalasUseCase.class);
        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));

        mockMvc = MockMvcBuilders.standaloneSetup(new MandalaController(listAvailableMandalasUseCase))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAvailableMandalas_returnsMappedResponse() throws Exception {
        when(listAvailableMandalasUseCase.execute(USER_ID)).thenReturn(List.of(AvailableMandala.builder()
                .mandala(Mandala.builder()
                        .id("mandala-01")
                        .title("Mandala 01")
                        .description("desc")
                        .assetKey("mandala-01")
                        .displayOrder(1)
                        .active(true)
                        .accessType(MandalaAccessType.FREE)
                        .build())
                .unlockSource(MandalaUnlockSource.FREE)
                .build()));

        mockMvc.perform(get("/api/mandalas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("mandala-01"))
                .andExpect(jsonPath("$[0].assetKey").value("mandala-01"))
                .andExpect(jsonPath("$[0].unlockSource").value("FREE"));
    }

    @Test
    void getAvailableMandalas_withoutAuthenticationReturnsUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/mandalas"))
                .andExpect(status().isUnauthorized());
    }
}
