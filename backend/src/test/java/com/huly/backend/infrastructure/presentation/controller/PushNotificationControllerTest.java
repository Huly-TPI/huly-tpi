package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusResponse;
import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionRequest;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.GetPushSubscriptionStatusUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.UpdateNotificationHourUseCase;
import com.huly.backend.infrastructure.presentation.mapper.pushNotification.PushNotificationPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PushNotificationControllerTest {

    private static final Long USER_ID = 1L;
    private static final String ENDPOINT = "https://fcm.example.com/1";
    private static final String P256DH = "key1";
    private static final String AUTH = "auth123";

    private MockMvc mockMvc;
    private SavePushSubscriptionUseCase savePushSubscriptionUseCase;
    private DeletePushSubscriptionUseCase deletePushSubscriptionUseCase;
    private GetPushSubscriptionStatusUseCase getPushSubscriptionStatusUseCase;
    private UpdateNotificationHourUseCase updateNotificationHourUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        savePushSubscriptionUseCase = mock(SavePushSubscriptionUseCase.class);
        deletePushSubscriptionUseCase = mock(DeletePushSubscriptionUseCase.class);
        getPushSubscriptionStatusUseCase = mock(GetPushSubscriptionStatusUseCase.class);
        updateNotificationHourUseCase = mock(UpdateNotificationHourUseCase.class);
        PushNotificationController controller = new PushNotificationController(
                savePushSubscriptionUseCase,
                deletePushSubscriptionUseCase,
                getPushSubscriptionStatusUseCase,
                new PushNotificationPresentationMapper(),
                updateNotificationHourUseCase);
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
    @DisplayName("Devuelve 201 cuando la suscripcion se guarda")
    void subscribeShouldReturn201WhenSubscriptionIsSaved() throws Exception {
        // --- act ---
        ResultActions result = performSubscribe();

        // --- assert ---
        thenCreated(result);
        thenSubscriptionSaved(new SavePushSubscriptionRequest(USER_ID, ENDPOINT, P256DH, AUTH));
    }

    @Test
    @DisplayName("Devuelve 204 cuando la suscripcion se elimina")
    void unsubscribeShouldReturn204WhenSubscriptionIsDeleted() throws Exception {
        // --- act ---
        ResultActions result = performUnsubscribe();

        // --- assert ---
        thenNoContent(result);
        thenSubscriptionDeleted(new DeletePushSubscriptionRequest(ENDPOINT));
    }

    @Test
    @DisplayName("Devuelve suscrito true cuando el usuario tiene suscripcion")
    void statusShouldReturnSubscribedTrueWhenUserHasSubscription() throws Exception {
        // --- arrange ---
        givenSubscriptionStatus(true, 20);

        // --- act ---
        ResultActions result = performGetStatus();

        // --- assert ---
        thenOkWithSubscribed(result, true);
    }

    @Test
    @DisplayName("Devuelve suscrito false cuando el usuario no tiene suscripcion")
    void statusShouldReturnSubscribedFalseWhenUserHasNotSubscription() throws Exception {
        // --- arrange ---
        givenSubscriptionStatus(false, 20);

        // --- act ---
        ResultActions result = performGetStatus();

        // --- assert ---
        thenOkWithSubscribed(result, false);
    }

    @Test
    @DisplayName("Devuelve 204 al actualizar la hora de notificacion")
    void updateHourShouldReturn204() throws Exception {
        // --- act ---
        ResultActions result = performUpdateHour(20);

        // --- assert ---
        thenNoContent(result);
        thenNotificationHourUpdated(USER_ID, 20);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenSubscriptionStatus(boolean subscribed, int hour) {
        when(getPushSubscriptionStatusUseCase.execute(new GetPushSubscriptionStatusRequest(USER_ID)))
                .thenReturn(new GetPushSubscriptionStatusResponse(subscribed, hour));
    }

    // --- act ---
    private ResultActions performSubscribe() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", 1,
                "endpoint", ENDPOINT,
                "p256dh", P256DH,
                "auth", AUTH);
        return mockMvc.perform(post("/api/pushNotification/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performUnsubscribe() throws Exception {
        Map<String, Object> body = Map.of("endpoint", ENDPOINT);
        return mockMvc.perform(delete("/api/pushNotification/unsubscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performGetStatus() throws Exception {
        return mockMvc.perform(get("/api/pushNotification/status"));
    }

    private ResultActions performUpdateHour(int hour) throws Exception {
        Map<String, Object> body = Map.of("hour", hour);
        return mockMvc.perform(put("/api/pushNotification/hour")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    // --- assert ---
    private void thenCreated(ResultActions result) throws Exception {
        result.andExpect(status().isCreated());
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenOkWithSubscribed(ResultActions result, boolean subscribed) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(subscribed));
    }

    private void thenSubscriptionSaved(SavePushSubscriptionRequest request) {
        verify(savePushSubscriptionUseCase).execute(eq(request));
    }

    private void thenSubscriptionDeleted(DeletePushSubscriptionRequest request) {
        verify(deletePushSubscriptionUseCase).execute(eq(request));
    }

    private void thenNotificationHourUpdated(Long userId, int hour) {
        verify(updateNotificationHourUseCase).execute(userId, hour);
    }
}
