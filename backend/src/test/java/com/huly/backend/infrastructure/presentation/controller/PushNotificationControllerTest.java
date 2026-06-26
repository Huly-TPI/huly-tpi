package com.huly.backend.infrastructure.presentation.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.dto.pushNotification.DeletePushSubscriptionRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusRequest;
import com.huly.backend.domain.dto.pushNotification.GetPushSubscriptionStatusResponse;
import com.huly.backend.domain.dto.pushNotification.SavePushSubscriptionRequest;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.GetPushSubscriptionStatusUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import com.huly.backend.infrastructure.presentation.mapper.pushNotification.PushNotificationPresentationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import java.util.Map;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class PushNotificationControllerTest {

    private MockMvc mockMvc;
    private SavePushSubscriptionUseCase savePushSubscriptionUseCase;
    private DeletePushSubscriptionUseCase deletePushSubscriptionUseCase;
    private GetPushSubscriptionStatusUseCase getPushSubscriptionStatusUseCase;
    private ObjectMapper objectMapper;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        savePushSubscriptionUseCase = mock(SavePushSubscriptionUseCase.class);
        deletePushSubscriptionUseCase = mock(DeletePushSubscriptionUseCase.class);
        getPushSubscriptionStatusUseCase = mock(GetPushSubscriptionStatusUseCase.class);
        objectMapper = new ObjectMapper();
        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        mockMvc = MockMvcBuilders.standaloneSetup(
                new PushNotificationController(
                        savePushSubscriptionUseCase,
                        deletePushSubscriptionUseCase,
                        getPushSubscriptionStatusUseCase,
                        new PushNotificationPresentationMapper()))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void subscribe_shouldReturn201_whenSubscriptionIsSaved() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", 1,
                "endpoint", "https://fcm.example.com/1",
                "p256dh", "key1",
                "auth", "auth123");

        mockMvc.perform(post("/api/pushNotification/subscribe").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        verify(savePushSubscriptionUseCase).execute(
                eq(new SavePushSubscriptionRequest(1L, "https://fcm.example.com/1", "key1", "auth123")));
    }

    @Test
    void unsubscribe_shouldReturn204_whenSubscriptionIsDeleted() throws Exception {
        Map<String, Object> body = Map.of(
                "endpoint", "https://fcm.example.com/1");

        mockMvc.perform(delete("/api/pushNotification/unsubscribe").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());

        verify(deletePushSubscriptionUseCase).execute(
                eq(new DeletePushSubscriptionRequest("https://fcm.example.com/1")));
    }

    @Test
    void status_shouldReturnSuscribedTrue_whenUserHasSuscription() throws Exception {
        when(getPushSubscriptionStatusUseCase.execute(new GetPushSubscriptionStatusRequest(USER_ID)))
                .thenReturn(new GetPushSubscriptionStatusResponse(true));

        mockMvc.perform(get("/api/pushNotification/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(true));
    }

    @Test
    void status_shouldReturnSuscribedFalse_whenUserHasNotSuscription() throws Exception {
        when(getPushSubscriptionStatusUseCase.execute(new GetPushSubscriptionStatusRequest(USER_ID)))
                .thenReturn(new GetPushSubscriptionStatusResponse(false));

        mockMvc.perform(get("/api/pushNotification/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(false));
    }

}
