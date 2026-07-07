package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.onboarding.GenerateOnboardingOptionsResponse;
import com.huly.backend.domain.useCase.onboarding.CompleteOnboardingUseCase;
import com.huly.backend.domain.useCase.onboarding.CompleteTutorialUseCase;
import com.huly.backend.domain.useCase.onboarding.GenerateOnboardingOptionsUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.onboarding.OnboardingPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OnboardingControllerTest {

    private static final Long USER_ID = 1L;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private GenerateOnboardingOptionsUseCase generateOnboardingOptionsUseCase;
    private CompleteOnboardingUseCase completeOnboardingUseCase;
    private CompleteTutorialUseCase completeTutorialUseCase;

    @BeforeEach
    void setUp() {
        generateOnboardingOptionsUseCase = mock(GenerateOnboardingOptionsUseCase.class);
        completeOnboardingUseCase = mock(CompleteOnboardingUseCase.class);
        completeTutorialUseCase = mock(CompleteTutorialUseCase.class);
        OnboardingController controller = new OnboardingController(
                generateOnboardingOptionsUseCase,
                completeOnboardingUseCase,
                completeTutorialUseCase,
                new OnboardingPresentationMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        authenticateAs(USER_ID);
    }

    @Test
    @DisplayName("Devuelve 200 con las opciones generadas del onboarding")
    void generateOptionsShouldReturn200WithOptions() throws Exception {
        // --- arrange ---
        givenGeneratedOptionsForStep2(generatedOptions());
        // --- act ---
        ResultActions result = performGenerateOptions(generateOptionsBody());
        // --- assert ---
        thenOkWithOptions(result);
    }

    @Test
    @DisplayName("Devuelve 400 al generar opciones cuando el paso es nulo")
    void generateOptionsShouldReturn400WhenStepIsNull() throws Exception {
        // --- act ---
        ResultActions result = performGenerateOptions(generateOptionsBodyWithoutStep());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 204 al completar el onboarding")
    void completeOnboardingShouldReturn204() throws Exception {
        // --- act ---
        ResultActions result = performComplete(completeBody());
        // --- assert ---
        thenNoContent(result);
        thenCompleteOnboardingCalledWithAnswers();
    }

    @Test
    @DisplayName("Devuelve 400 al completar el onboarding cuando una respuesta está vacía")
    void completeOnboardingShouldReturn400WhenAnswerIsBlank() throws Exception {
        // --- act ---
        ResultActions result = performComplete(completeBodyMissingAnswer());
        // --- assert ---
        thenBadRequest(result);
    }

    @Test
    @DisplayName("Devuelve 204 al completar el tutorial")
    void completeTutorialShouldReturn204() throws Exception {
        // --- act ---
        ResultActions result = performCompleteTutorial();
        // --- assert ---
        thenNoContent(result);
        thenCompleteTutorialCalledWithUser();
    }

    @Test
    @DisplayName("Devuelve 204 al completar el tutorial de perfil")
    void completeProfileTutorialShouldReturn204() throws Exception {
        // --- act ---
        ResultActions result = performCompleteProfileTutorial();
        // --- assert ---
        thenNoContent(result);
        thenCompleteProfileTutorialCalledWithUser();
    }

    // --- arrange ---
    private void authenticateAs(Long userId) {
        User user = new User(String.valueOf(userId), "password", List.of(new SimpleGrantedAuthority("USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    private void givenGeneratedOptionsForStep2(GenerateOnboardingOptionsResponse response) {
        when(generateOnboardingOptionsUseCase.execute(argThat(req ->
                req.step() == 2 && "Desestresarme".equals(req.previousAnswer()))))
                .thenReturn(response);
    }

    private GenerateOnboardingOptionsResponse generatedOptions() {
        return new GenerateOnboardingOptionsResponse(List.of("Meditar", "Caminar", "Respirar", "Leer"));
    }

    private Map<String, Object> generateOptionsBody() {
        return Map.of("step", 2, "previousAnswer", "Desestresarme");
    }

    private Map<String, Object> generateOptionsBodyWithoutStep() {
        return Map.of("previousAnswer", "Desestresarme");
    }

    private Map<String, Object> completeBody() {
        return Map.of("answer1", "Desestresarme", "answer2", "Meditar", "answer3", "Meditar 5 minutos");
    }

    private Map<String, Object> completeBodyMissingAnswer() {
        return Map.of("answer1", "Desestresarme", "answer2", "Meditar");
    }

    // --- act ---
    private ResultActions performGenerateOptions(Map<String, Object> body) throws Exception {
        return mockMvc.perform(post("/api/onboarding/generate-options")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performComplete(Map<String, Object> body) throws Exception {
        return mockMvc.perform(post("/api/onboarding/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performCompleteTutorial() throws Exception {
        return mockMvc.perform(post("/api/onboarding/tutorial/complete"));
    }

    private ResultActions performCompleteProfileTutorial() throws Exception {
        return mockMvc.perform(post("/api/onboarding/profile-onboarding-tutorial/complete"));
    }

    // --- assert ---
    private void thenOkWithOptions(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.options").isArray())
                .andExpect(jsonPath("$.options[0]").value("Meditar"))
                .andExpect(jsonPath("$.options[1]").value("Caminar"));
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenCompleteOnboardingCalledWithAnswers() {
        verify(completeOnboardingUseCase).execute(argThat(req ->
                req.userId().equals(USER_ID)
                        && "Desestresarme".equals(req.answer1())
                        && "Meditar".equals(req.answer2())
                        && "Meditar 5 minutos".equals(req.answer3())));
    }

    private void thenCompleteTutorialCalledWithUser() {
        verify(completeTutorialUseCase).execute(argThat(req -> req.userId().equals(USER_ID)));
    }

    private void thenCompleteProfileTutorialCalledWithUser() {
        verify(completeTutorialUseCase).executeProfile(argThat(req -> req.userId().equals(USER_ID)));
    }
}
