package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.useCase.payment.HandleWebhookUseCase;
import com.huly.backend.infrastructure.adapter.mercadopago.MercadoPagoSignatureValidator;
import com.huly.backend.infrastructure.presentation.controller.MercadoPagoWebhookController;
import com.huly.backend.infrastructure.presentation.dto.payment.WebhookNotificationDto;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MercadoPagoWebhookControllerTest {

    private MockMvc mockMvc;
    private HandleWebhookUseCase handleWebhookUseCase;
    private MercadoPagoSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String VALID_SIGNATURE = "ts=1718000000,v1=abc123";
    private static final String VALID_REQUEST_ID = "req-abc-123";

    @BeforeEach
    void setUp() {
        handleWebhookUseCase = mock(HandleWebhookUseCase.class);
        signatureValidator = mock(MercadoPagoSignatureValidator.class);

        MercadoPagoWebhookController controller =
                new MercadoPagoWebhookController(handleWebhookUseCase, signatureValidator);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);
    }

    @Test
    void handleWebhook_shouldReturn401_whenSignatureHeaderIsMissing() throws Exception {
        when(signatureValidator.isValid(any(), any(), any())).thenReturn(false);
        WebhookNotificationDto notification = paymentNotification("99");

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(handleWebhookUseCase);
    }

    @Test
    void handleWebhook_shouldReturn401_whenSignatureIsInvalid() throws Exception {
        when(signatureValidator.isValid(any(), any(), any())).thenReturn(false);
        WebhookNotificationDto notification = paymentNotification("99");

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", "ts=1234,v1=wronghash")
                        .header("x-request-id", VALID_REQUEST_ID)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(handleWebhookUseCase);
    }

    @Test
    void handleWebhook_shouldReturn200_whenTypeIsNotPayment() throws Exception {
        WebhookNotificationDto notification = new WebhookNotificationDto(
                1L, true, "merchant_order", null, null, null, "created",
                new WebhookNotificationDto.WebhookDataDto("456"));

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", VALID_SIGNATURE)
                        .header("x-request-id", VALID_REQUEST_ID)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        verifyNoInteractions(handleWebhookUseCase);
    }

    @Test
    void handleWebhook_shouldReturn200_whenDataIsNull() throws Exception {
        WebhookNotificationDto notification = new WebhookNotificationDto(
                1L, true, "payment", null, null, null, "payment.created", null);

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", VALID_SIGNATURE)
                        .header("x-request-id", VALID_REQUEST_ID)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        verifyNoInteractions(handleWebhookUseCase);
    }

    @Test
    void handleWebhook_shouldReturn400_whenPaymentIdIsNotNumeric() throws Exception {
        WebhookNotificationDto notification = new WebhookNotificationDto(
                1L, true, "payment", null, null, null, "payment.updated",
                new WebhookNotificationDto.WebhookDataDto("not-a-number"));

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", VALID_SIGNATURE)
                        .header("x-request-id", VALID_REQUEST_ID)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(handleWebhookUseCase);
    }

    @Test
    void handleWebhook_shouldReturn200_whenPaymentProcessedSuccessfully() throws Exception {
        WebhookNotificationDto notification = paymentNotification("99");

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", VALID_SIGNATURE)
                        .header("x-request-id", VALID_REQUEST_ID)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());

        verify(handleWebhookUseCase).execute(99L);
    }

    @Test
    void handleWebhook_shouldReturn500_whenUseCaseThrowsException() throws Exception {
        WebhookNotificationDto notification = paymentNotification("99");
        doThrow(new RuntimeException("DB error")).when(handleWebhookUseCase).execute(99L);

        mockMvc.perform(post("/api/webhook/mercadopago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("x-signature", VALID_SIGNATURE)
                        .header("x-request-id", VALID_REQUEST_ID)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isInternalServerError());
    }

    private WebhookNotificationDto paymentNotification(String dataId) {
        return new WebhookNotificationDto(
                1L, true, "payment", null, null, null, "payment.updated",
                new WebhookNotificationDto.WebhookDataDto(dataId));
    }
}
