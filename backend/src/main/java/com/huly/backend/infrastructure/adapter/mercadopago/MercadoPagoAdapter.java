package com.huly.backend.infrastructure.adapter.mercadopago;

import com.huly.backend.domain.dto.payment.MercadoPagoPaymentResult;
import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MercadoPagoAdapter implements MercadoPagoPort {

    @Value("${mercadopago.webhook-url}")
    private String webhookUrl;

    /** URL del frontend para volver al sitio luego del pago (botón "Volver al sitio"). */
    @Value("${frontend.url:}")
    private String frontendUrl;

    /**
     * Estado que dispara la redirección automática ("redireccionando en N seg").
     * Por defecto "approved"; se puede vaciar (MERCADOPAGO_AUTO_RETURN=) para
     * desactivarla (ej. en local, donde MP puede rechazar URLs no públicas).
     */
    @Value("${mercadopago.auto-return:approved}")
    private String autoReturn;

    @Override
    public PaymentPreferenceResult createPreference(Product product, Long userId, String externalReference) {
        log.info("Creating MercadoPago preference for product: id={} name={} price={}",
                product.getId(), product.getName(), product.getPrice());
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(String.valueOf(product.getId()))
                    .title(product.getName())
                    .description(product.getDescription())
                    .quantity(1)
                    .unitPrice(product.getPrice())
                    .build();

            var builder = PreferenceRequest.builder()
                    .items(List.of(item))
                    .notificationUrl(webhookUrl)
                    .externalReference(externalReference)
                    .metadata(Map.of("user_id", userId));

            // Botón "Volver al sitio" + redirección automática tras el pago.
            if (frontendUrl != null && !frontendUrl.isBlank()) {
                String shopUrl = frontendUrl + "/shop";
                builder.backUrls(PreferenceBackUrlsRequest.builder()
                        .success(shopUrl + "?payment=success")
                        .pending(shopUrl + "?payment=pending")
                        .failure(shopUrl + "?payment=failure")
                        .build());
                // auto_return solo es válido con una URL de éxito pública (https, no localhost);
                // con localhost MercadoPago rechaza la preferencia (400).
                if (autoReturn != null && !autoReturn.isBlank() && isPublicHttps(frontendUrl)) {
                    builder.autoReturn(autoReturn);
                } else if (autoReturn != null && !autoReturn.isBlank()) {
                    log.warn("Skipping auto_return: frontend.url no es https pública ({}). El botón 'Volver al sitio' sí se incluye.", frontendUrl);
                }
            }

            PreferenceRequest request = builder.build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            log.info("Preference created: id={} initPoint={}", preference.getId(), preference.getInitPoint());
            return new PaymentPreferenceResult(preference.getId(), preference.getInitPoint());

        } catch (MPApiException e) {
            log.error("MP API Error creating preference for product={} | status={} | body={}",
                    product.getId(), e.getStatusCode(),
                    e.getApiResponse() != null ? e.getApiResponse().getContent() : "no body");
            throw new RuntimeException("Error al crear la preferencia de pago", e);
        } catch (MPException e) {
            log.error("MP Exception creating preference for product={} | message={}", product.getId(), e.getMessage());
            throw new RuntimeException("Error al crear la preferencia de pago", e);
        }
    }

    /** MercadoPago necesita una URL pública (https, no localhost) para usar auto_return. */
    private boolean isPublicHttps(String url) {
        return url.startsWith("https://")
                && !url.contains("localhost")
                && !url.contains("127.0.0.1");
    }

    @Override
    public MercadoPagoPaymentResult getPayment(Long paymentId) {
        log.info("Querying MP payment id={}", paymentId);
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(paymentId);
            return new MercadoPagoPaymentResult(
                    payment.getId(),
                    payment.getExternalReference(),
                    payment.getStatus(),
                    payment.getStatusDetail()
            );
        } catch (MPApiException e) {
            log.error("MP API Error getting payment={} | status={} | body={}",
                    paymentId, e.getStatusCode(),
                    e.getApiResponse() != null ? e.getApiResponse().getContent() : "no body");
            throw new RuntimeException("Error al consultar el pago", e);
        } catch (MPException e) {
            log.error("MP Exception getting payment={} | message={}", paymentId, e.getMessage());
            throw new RuntimeException("Error al consultar el pago", e);
        }
    }
}
