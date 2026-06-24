package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.enums.MandalaAccessType;
import com.huly.backend.domain.model.enums.MandalaUnlockSource;
import com.huly.backend.domain.model.mandala.AvailableMandala;
import com.huly.backend.domain.model.mandala.Mandala;
import com.huly.backend.domain.useCase.mandala.ClearMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.GetMandalaProgressUseCase;
import com.huly.backend.domain.useCase.mandala.ListAvailableMandalasUseCase;
import com.huly.backend.domain.useCase.mandala.SaveMandalaProgressUseCase;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MandalaControllerTest {

    private static final Long USER_ID = 7L;

    private MockMvc mockMvc;
    private ListAvailableMandalasUseCase listAvailableMandalasUseCase;
    private SaveMandalaProgressUseCase saveMandalaProgressUseCase;
    private GetMandalaProgressUseCase getMandalaProgressUseCase;
    private ClearMandalaProgressUseCase clearMandalaProgressUseCase;

    @BeforeEach
    void setUp() {
        listAvailableMandalasUseCase = mock(ListAvailableMandalasUseCase.class);
        saveMandalaProgressUseCase = mock(SaveMandalaProgressUseCase.class);
        getMandalaProgressUseCase = mock(GetMandalaProgressUseCase.class);
        clearMandalaProgressUseCase = mock(ClearMandalaProgressUseCase.class);
        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new MandalaController(listAvailableMandalasUseCase, saveMandalaProgressUseCase,
                        getMandalaProgressUseCase, clearMandalaProgressUseCase))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAvailableMandalas_withoutAuthenticationReturnsUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/mandalas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveProgress_delegatesBinaryBodyToUseCase() throws Exception {
        byte[] paintBlob = "paint".getBytes();

        mockMvc.perform(put("/api/mandalas/mandala-01/progress")
                .contentType("application/octet-stream")
                .content(paintBlob))
                .andExpect(status().isOk());

        verify(saveMandalaProgressUseCase).execute(USER_ID, "mandala-01", paintBlob);
    }

    @Test
    void getProgress_returnsStoredBinaryBody() throws Exception {
        byte[] paintBlob = "paint".getBytes();
        when(getMandalaProgressUseCase.execute(USER_ID, "mandala-01")).thenReturn(Optional.of(paintBlob));

        mockMvc.perform(get("/api/mandalas/mandala-01/progress"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(paintBlob));
    }

    @Test
    void getProgress_withoutStoredProgressReturnsNotFound() throws Exception {
        when(getMandalaProgressUseCase.execute(USER_ID, "mandala-01")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/mandalas/mandala-01/progress"))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearProgress_delegatesToUseCase() throws Exception {
        mockMvc.perform(delete("/api/mandalas/mandala-01/progress"))
                .andExpect(status().isNoContent());

        verify(clearMandalaProgressUseCase).execute(USER_ID, "mandala-01");
    }
}
