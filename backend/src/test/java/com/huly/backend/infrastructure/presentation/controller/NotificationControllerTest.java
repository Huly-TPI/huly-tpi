package com.huly.backend.infrastructure.presentation.controller;
import com.huly.backend.domain.useCase.pushNotification.UnsubscribeFromEmailsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private MockMvc mockMvc;
    private UnsubscribeFromEmailsUseCase unsubscribeFromEmailsUseCase;

    @BeforeEach
    void setUp() {
        unsubscribeFromEmailsUseCase = mock(UnsubscribeFromEmailsUseCase.class);
        NotificationController controller = new NotificationController(unsubscribeFromEmailsUseCase);
        ReflectionTestUtils.setField(controller, "frontendUrl", "http://localhost:5173");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test 
    void unsubscribe_shouldRedirectWithOk_whenTokenIsValid() throws Exception {
         when(unsubscribeFromEmailsUseCase.execute("tok-123")).thenReturn(true);
         mockMvc.perform(get("/api/notifications/unsubscribe").param("token", "tok-123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/unsubscribe?status=ok"));
        verify(unsubscribeFromEmailsUseCase).execute("tok-123");
    }

    @Test
    void unsubscribe_shouldRedirectWithError_whenTokenIsInvalid() throws Exception {
        when(unsubscribeFromEmailsUseCase.execute("bad")).thenReturn(false);
        mockMvc.perform(get("/api/notifications/unsubscribe").param("token", "bad"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:5173/unsubscribe?status=error"));
    }
    
}
