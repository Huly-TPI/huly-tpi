package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.useCase.payment.HandleWebhookUseCase;
import com.huly.backend.infrastructure.adapter.mercadopago.MercadoPagoSignatureValidator;
import com.huly.backend.infrastructure.presentation.dto.payment.WebhookNotificationDto;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MercadoPagoWebhookControllerTest {

    private static final String VALID_SIGNATURE = "ts=1718000000,v1=abc123";
    private static final String VALID_REQUEST_ID = "req-abc-123";

    private MockMvc mockMvc;
    private HandleWebhookUseCase handleWebhookUseCase;
    private MercadoPagoSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        handleWebhookUseCase = mock(HandleWebhookUseCase.class);
        signatureValidator = mock(MercadoPagoSignatureValidator.class);
        MercadoPagoWebhookController controller =
                new MercadoPagoWebhookController(handleWebhookUseCase, signatureValidator);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        givenValidSignature();
    }

    @Test
    @DisplayName("Devuelve 401 cuando faltan las cabeceras de firma")
    void handleWebhookShouldReturn401WhenSignatureHeaderIsMissing() throws Exception {
        // --- arrange ---
        givenInvalidSignature();

        // --- act ---
        ResultActions result = performWebhookWithoutSignatureHeaders(paymentNotification("99"));

        // --- assert ---
        thenUnauthorized(result);
        thenWebhookUseCaseNotInvoked();
    }

    @Test
    @DisplayName("Devuelve 401 cuando la firma es invalida")
    void handleWebhookShouldReturn401WhenSignatureIsInvalid() throws Exception {
        // --- arrange ---
        givenInvalidSignature();

        // --- act ---
        ResultActions result = performWebhookWithHeaders(
                "ts=1234,v1=wronghash", VALID_REQUEST_ID, paymentNotification("99"));

        // --- assert ---
        thenUnauthorized(result);
        thenWebhookUseCaseNotInvoked();
    }

    @Test
    @DisplayName("Devuelve 200 cuando el tipo de notificacion no es payment")
    void handleWebhookShouldReturn200WhenTypeIsNotPayment() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = merchantOrderNotification("456");

        // --- act ---
        ResultActions result = performWebhook(notification);

        // --- assert ---
        thenOk(result);
        thenWebhookUseCaseNotInvoked();
    }

    @Test
    @DisplayName("Devuelve 200 cuando el cuerpo de la notificacion no trae data")
    void handleWebhookShouldReturn200WhenDataIsNull() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = paymentNotificationWithoutData();

        // --- act ---
        ResultActions result = performWebhook(notification);

        // --- assert ---
        thenOk(result);
        thenWebhookUseCaseNotInvoked();
    }

    @Test
    @DisplayName("Devuelve 200 cuando la data de payment no trae id")
    void handleWebhookShouldReturn200WhenPaymentDataIdIsNull() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = paymentNotification(null);

        // --- act ---
        ResultActions result = performWebhook(notification);

        // --- assert ---
        thenOk(result);
        thenWebhookUseCaseNotInvoked();
    }

    @Test
    @DisplayName("Devuelve 400 cuando el id de payment no es numerico")
    void handleWebhookShouldReturn400WhenPaymentIdIsNotNumeric() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = paymentNotification("not-a-number");

        // --- act ---
        ResultActions result = performWebhook(notification);

        // --- assert ---
        thenBadRequest(result);
        thenWebhookUseCaseNotInvoked();
    }

    @Test
    @DisplayName("Devuelve 200 cuando el payment se procesa correctamente")
    void handleWebhookShouldReturn200WhenPaymentProcessedSuccessfully() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = paymentNotification("99");

        // --- act ---
        ResultActions result = performWebhook(notification);

        // --- assert ---
        thenOk(result);
        thenWebhookProcessedFor(99L);
    }

    @Test
    @DisplayName("Devuelve 200 usando el id recibido por query param data.id")
    void handleWebhookShouldReturn200WhenDataIdRequestParamIsPresent() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = paymentNotification("99");

        // --- act ---
        ResultActions result = performWebhookWithDataIdParam("99", notification);

        // --- assert ---
        thenOk(result);
        thenWebhookProcessedFor(99L);
    }

    @Test
    @DisplayName("Devuelve 500 cuando el use case lanza una excepcion")
    void handleWebhookShouldReturn500WhenUseCaseThrowsException() throws Exception {
        // --- arrange ---
        WebhookNotificationDto notification = paymentNotification("99");
        givenUseCaseFailsFor(99L);

        // --- act ---
        ResultActions result = performWebhook(notification);

        // --- assert ---
        thenInternalServerError(result);
    }

    // --- arrange ---
    private void givenValidSignature() {
        when(signatureValidator.isValid(any(), any(), any())).thenReturn(true);
    }

    private void givenInvalidSignature() {
        when(signatureValidator.isValid(any(), any(), any())).thenReturn(false);
    }

    private void givenUseCaseFailsFor(Long paymentId) {
        doThrow(new RuntimeException("DB error")).when(handleWebhookUseCase).execute(paymentId);
    }

    private WebhookNotificationDto paymentNotification(String dataId) {
        return new WebhookNotificationDto(
                1L, true, "payment", null, null, null, "payment.updated",
                new WebhookNotificationDto.WebhookDataDto(dataId));
    }

    private WebhookNotificationDto paymentNotificationWithoutData() {
        return new WebhookNotificationDto(
                1L, true, "payment", null, null, null, "payment.created", null);
    }

    private WebhookNotificationDto merchantOrderNotification(String dataId) {
        return new WebhookNotificationDto(
                1L, true, "merchant_order", null, null, null, "created",
                new WebhookNotificationDto.WebhookDataDto(dataId));
    }

    // --- act ---
    private ResultActions performWebhook(WebhookNotificationDto notification) throws Exception {
        return performWebhookWithHeaders(VALID_SIGNATURE, VALID_REQUEST_ID, notification);
    }

    private ResultActions performWebhookWithHeaders(
            String signature, String requestId, WebhookNotificationDto notification) throws Exception {
        return mockMvc.perform(post("/api/webhook/mercadopago")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-signature", signature)
                .header("x-request-id", requestId)
                .content(objectMapper.writeValueAsString(notification)));
    }

    private ResultActions performWebhookWithoutSignatureHeaders(
            WebhookNotificationDto notification) throws Exception {
        return mockMvc.perform(post("/api/webhook/mercadopago")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)));
    }

    private ResultActions performWebhookWithDataIdParam(
            String dataId, WebhookNotificationDto notification) throws Exception {
        return mockMvc.perform(post("/api/webhook/mercadopago")
                .contentType(MediaType.APPLICATION_JSON)
                .header("x-signature", VALID_SIGNATURE)
                .header("x-request-id", VALID_REQUEST_ID)
                .param("data.id", dataId)
                .content(objectMapper.writeValueAsString(notification)));
    }

    // --- assert ---
    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenBadRequest(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest());
    }

    private void thenUnauthorized(ResultActions result) throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    private void thenInternalServerError(ResultActions result) throws Exception {
        result.andExpect(status().isInternalServerError());
    }

    private void thenWebhookProcessedFor(Long paymentId) {
        verify(handleWebhookUseCase).execute(paymentId);
    }

    private void thenWebhookUseCaseNotInvoked() {
        verifyNoInteractions(handleWebhookUseCase);
    }
}
