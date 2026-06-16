package com.huly.backend.infrastructure.adapter.mercadopago;

import com.huly.backend.domain.dto.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class MercadoPagoAdapterTest {

    private MercadoPagoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MercadoPagoAdapter();
        ReflectionTestUtils.setField(adapter, "webhookUrl", "https://api.example.com/webhook/mercadopago");
    }

    private Product product() {
        return Product.builder()
                .id(1L)
                .name("Pack Inicial")
                .description("100 monedas")
                .price(new BigDecimal("499.00"))
                .build();
    }

    private Preference mockedPreference() throws Exception {
        Preference preference = mock(Preference.class);
        when(preference.getId()).thenReturn("pref-123");
        when(preference.getInitPoint()).thenReturn("https://mp.com/init");
        return preference;
    }

    @Test
    void createPreference_shouldReturnResult_withAutoReturn_whenFrontendUrlIsPublicHttps() throws Exception {
        ReflectionTestUtils.setField(adapter, "frontendUrl", "https://app.example.com");
        ReflectionTestUtils.setField(adapter, "autoReturn", "approved");
        Preference preference = mockedPreference();

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (clientMock, ctx) -> when(clientMock.create(any(PreferenceRequest.class))).thenReturn(preference))) {

            PaymentPreferenceResult result = adapter.createPreference(product(), 10L, "ext-ref");

            assertThat(result.getId()).isEqualTo("pref-123");
            assertThat(result.getInitPoint()).isEqualTo("https://mp.com/init");
            assertThat(mocked.constructed()).hasSize(1);
        }
    }

    @Test
    void createPreference_shouldSkipAutoReturn_whenFrontendUrlIsLocalhost() throws Exception {
        ReflectionTestUtils.setField(adapter, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(adapter, "autoReturn", "approved");
        Preference preference = mockedPreference();

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (clientMock, ctx) -> when(clientMock.create(any(PreferenceRequest.class))).thenReturn(preference))) {

            PaymentPreferenceResult result = adapter.createPreference(product(), 10L, "ext-ref");

            assertThat(result.getInitPoint()).isEqualTo("https://mp.com/init");
        }
    }

    @Test
    void createPreference_shouldOmitBackUrls_whenFrontendUrlIsBlank() throws Exception {
        ReflectionTestUtils.setField(adapter, "frontendUrl", "");
        ReflectionTestUtils.setField(adapter, "autoReturn", "approved");
        Preference preference = mockedPreference();

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (clientMock, ctx) -> when(clientMock.create(any(PreferenceRequest.class))).thenReturn(preference))) {

            PaymentPreferenceResult result = adapter.createPreference(product(), 10L, "ext-ref");

            assertThat(result.getId()).isEqualTo("pref-123");
        }
    }

    @Test
    void createPreference_shouldThrowRuntimeException_whenMPApiExceptionOccurs() {
        ReflectionTestUtils.setField(adapter, "frontendUrl", "https://app.example.com");
        ReflectionTestUtils.setField(adapter, "autoReturn", "approved");

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (clientMock, ctx) -> when(clientMock.create(any(PreferenceRequest.class)))
                        .thenThrow(new MPApiException("api error", new MPResponse(400, Map.of(), "bad request"))))) {

            assertThatThrownBy(() -> adapter.createPreference(product(), 10L, "ext-ref"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al crear la preferencia de pago");
        }
    }

    @Test
    void createPreference_shouldThrowRuntimeException_whenMPExceptionOccurs() {
        ReflectionTestUtils.setField(adapter, "frontendUrl", "https://app.example.com");
        ReflectionTestUtils.setField(adapter, "autoReturn", "approved");

        try (MockedConstruction<PreferenceClient> mocked = mockConstruction(PreferenceClient.class,
                (clientMock, ctx) -> when(clientMock.create(any(PreferenceRequest.class)))
                        .thenThrow(new MPException("boom")))) {

            assertThatThrownBy(() -> adapter.createPreference(product(), 10L, "ext-ref"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al crear la preferencia de pago");
        }
    }

    @Test
    void getPayment_shouldReturnResult() throws Exception {
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(123L);
        when(payment.getExternalReference()).thenReturn("ext-ref");
        when(payment.getStatus()).thenReturn("approved");
        when(payment.getStatusDetail()).thenReturn("accredited");

        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (clientMock, ctx) -> when(clientMock.get(anyLong())).thenReturn(payment))) {

            MercadoPagoPaymentResult result = adapter.getPayment(123L);

            assertThat(result.getPaymentId()).isEqualTo(123L);
            assertThat(result.getExternalReference()).isEqualTo("ext-ref");
            assertThat(result.getStatus()).isEqualTo("approved");
            assertThat(result.getStatusDetail()).isEqualTo("accredited");
        }
    }

    @Test
    void getPayment_shouldThrowRuntimeException_whenMPApiExceptionOccurs() {
        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (clientMock, ctx) -> when(clientMock.get(anyLong()))
                        .thenThrow(new MPApiException("api error", new MPResponse(404, Map.of(), "not found"))))) {

            assertThatThrownBy(() -> adapter.getPayment(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al consultar el pago");
        }
    }

    @Test
    void getPayment_shouldThrowRuntimeException_whenMPExceptionOccurs() {
        try (MockedConstruction<PaymentClient> mocked = mockConstruction(PaymentClient.class,
                (clientMock, ctx) -> when(clientMock.get(anyLong())).thenThrow(new MPException("boom")))) {

            assertThatThrownBy(() -> adapter.getPayment(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error al consultar el pago");
        }
    }
}
