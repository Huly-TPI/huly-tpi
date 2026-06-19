package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.useCase.onboarding.CompleteOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteTutorialUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import com.huly.backend.infrastructure.presentation.controller.OnboardingController;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OnboardingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;
    private CompleteOnboardingUseCase completeOnboardingUseCase;
    private CompleteTutorialUseCase completeTutorialUseCase;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        generateOnboardingOptionsUseCase = mock(GenerateOnboardingOptionsUseCase.class);
        completeOnboardingUseCase = mock(CompleteOnboardingUseCase.class);
        completeTutorialUseCase = mock(CompleteTutorialUseCase.class);
        OnboardingController controller = new OnboardingController(
                generateOnboardingOptionsUseCase,
                completeOnboardingUseCase,
                completeTutorialUseCase
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        User user = new User(String.valueOf(USER_ID), "password", List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    @Test
    void generateOptions_shouldReturn200WithOptions() throws Exception {
        when(generateOnboardingOptionsUseCase.execute(2, "Desestresarme"))
                .thenReturn(List.of("Meditar", "Caminar", "Respirar", "Leer"));

        mockMvc.perform(post("/api/onboarding/generate-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("step", 2, "previousAnswer", "Desestresarme"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.options").isArray())
                .andExpect(jsonPath("$.options[0]").value("Meditar"))
                .andExpect(jsonPath("$.options[1]").value("Caminar"));
    }

    @Test
    void generateOptions_shouldReturn400_whenStepIsNull() throws Exception {
        mockMvc.perform(post("/api/onboarding/generate-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("previousAnswer", "Desestresarme"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeOnboarding_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/onboarding/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("answer1", "Desestresarme",
                                        "answer2", "Meditar",
                                        "answer3", "Meditar 5 minutos"))))
                .andExpect(status().isNoContent());

        verify(completeOnboardingUseCase).execute(USER_ID, "Desestresarme", "Meditar", "Meditar 5 minutos");
    }

    @Test
    void completeOnboarding_shouldReturn400_whenAnswerIsBlank() throws Exception {
        mockMvc.perform(post("/api/onboarding/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("answer1", "Desestresarme",
                                        "answer2", "Meditar"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeTutorial_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/onboarding/tutorial/complete"))
                .andExpect(status().isNoContent());

        verify(completeTutorialUseCase).execute(USER_ID);
    }

    @Test
    void completeProfileTutorial_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/onboarding/profile-onboarding-tutorial/complete"))
                .andExpect(status().isNoContent());

        verify(completeTutorialUseCase).executeProfile(USER_ID);
    }
}
