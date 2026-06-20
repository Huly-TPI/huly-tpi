package com.huly.backend.infrastructure.presentation.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
class PushNotificationControllerTest {

    private MockMvc mockMvc;
    private SavePushSubscriptionUseCase savePushSubscriptionUseCase;
    private DeletePushSubscriptionUseCase deletePushSubscriptionUseCase;
    private ObjectMapper objectMapper;
    @BeforeEach
    void setUp() {
        savePushSubscriptionUseCase = mock(SavePushSubscriptionUseCase.class);
        deletePushSubscriptionUseCase = mock(DeletePushSubscriptionUseCase.class);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new PushNotificationController(savePushSubscriptionUseCase, deletePushSubscriptionUseCase)).build();
    }

    @Test
    void subscribe_shouldReturn201_whenSubscriptionIsSaved() throws Exception {
        Map<String, Object> body = Map.of(
            "userId", 1, 
            "endpoint", "https://fcm.example.com/1", 
            "p256dh", "key1", 
            "auth", "auth123" ); 

        mockMvc.perform(post("/api/pushNotification/subscribe").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated());   

        verify(savePushSubscriptionUseCase).execute(eq(1L), eq("https://fcm.example.com/1"), eq("key1"), eq("auth123"));
    }

    @Test 
    void unsubscribe_shouldReturn204_whenSubscriptionIsDeleted() throws Exception {
        Map<String, Object> body = Map.of(
            "endpoint", "https://fcm.example.com/1");

        mockMvc.perform(delete("/api/pushNotification/unsubscribe").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isNoContent());

        verify(deletePushSubscriptionUseCase).execute(eq("https://fcm.example.com/1"));
    }
    
}
