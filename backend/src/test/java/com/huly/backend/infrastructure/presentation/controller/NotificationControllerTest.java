package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsRequest;
import com.huly.backend.domain.dto.pushNotification.UnsubscribeFromEmailsResponse;
import com.huly.backend.domain.useCase.pushNotification.UnsubscribeFromEmailsUseCase;
import com.huly.backend.infrastructure.presentation.mapper.notification.NotificationPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    private MockMvc mockMvc;
    private UnsubscribeFromEmailsUseCase unsubscribeFromEmailsUseCase;

    @BeforeEach
    void setUp() {
        unsubscribeFromEmailsUseCase = mock(UnsubscribeFromEmailsUseCase.class);
        NotificationController controller = new NotificationController(
                unsubscribeFromEmailsUseCase, new NotificationPresentationMapper());
        ReflectionTestUtils.setField(controller, "frontendUrl", FRONTEND_URL);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Redirige con estado ok cuando el token es valido")
    void unsubscribeShouldRedirectWithOkWhenTokenIsValid() throws Exception {
        // --- arrange ---
        givenUnsubscribeResult("tok-123", true);

        // --- act ---
        ResultActions result = performUnsubscribe("tok-123");

        // --- assert ---
        thenRedirectedTo(result, FRONTEND_URL + "/unsubscribe?status=ok");
        thenUnsubscribeExecutedFor("tok-123");
    }

    @Test
    @DisplayName("Redirige con estado error cuando el token es invalido")
    void unsubscribeShouldRedirectWithErrorWhenTokenIsInvalid() throws Exception {
        // --- arrange ---
        givenUnsubscribeResult("bad", false);

        // --- act ---
        ResultActions result = performUnsubscribe("bad");

        // --- assert ---
        thenRedirectedTo(result, FRONTEND_URL + "/unsubscribe?status=error");
    }

    // --- arrange ---
    private void givenUnsubscribeResult(String token, boolean success) {
        when(unsubscribeFromEmailsUseCase.execute(new UnsubscribeFromEmailsRequest(token)))
                .thenReturn(new UnsubscribeFromEmailsResponse(success));
    }

    // --- act ---
    private ResultActions performUnsubscribe(String token) throws Exception {
        return mockMvc.perform(get("/api/notifications/unsubscribe").param("token", token));
    }

    // --- assert ---
    private void thenRedirectedTo(ResultActions result, String location) throws Exception {
        result.andExpect(status().isFound())
                .andExpect(header().string("Location", location));
    }

    private void thenUnsubscribeExecutedFor(String token) {
        verify(unsubscribeFromEmailsUseCase).execute(new UnsubscribeFromEmailsRequest(token));
    }
}
