package com.huly.backend.infrastructure.adapter.mercadopago;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MercadoPagoSignatureValidatorTest {

    private MercadoPagoSignatureValidator validator;

    private static final String SECRET = "test-webhook-secret";
    private static final String DATA_ID = "99";
    private static final String REQUEST_ID = "req-abc-123";
    private static final String TS = "1718000000";

    @BeforeEach
    void setUp() {
        validator = new MercadoPagoSignatureValidator();
        ReflectionTestUtils.setField(validator, "webhookSecret", SECRET);
    }

    @Test
    void isValid_shouldReturnTrue_whenSignatureIsCorrect() {
        String v1 = computeHmac(SECRET, DATA_ID, REQUEST_ID, TS);
        String xSignature = "ts=" + TS + ",v1=" + v1;

        assertThat(validator.isValid(xSignature, REQUEST_ID, DATA_ID)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenSignatureIsWrong() {
        String xSignature = "ts=" + TS + ",v1=invalidhmacvalue";

        assertThat(validator.isValid(xSignature, REQUEST_ID, DATA_ID)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenXSignatureIsNull() {
        assertThat(validator.isValid(null, REQUEST_ID, DATA_ID)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenXRequestIdIsNull() {
        String v1 = computeHmac(SECRET, DATA_ID, REQUEST_ID, TS);
        assertThat(validator.isValid("ts=" + TS + ",v1=" + v1, null, DATA_ID)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenDataIdIsNull() {
        String v1 = computeHmac(SECRET, DATA_ID, REQUEST_ID, TS);
        assertThat(validator.isValid("ts=" + TS + ",v1=" + v1, REQUEST_ID, null)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenXSignatureIsMissingTsPart() {
        String v1 = computeHmac(SECRET, DATA_ID, REQUEST_ID, TS);
        assertThat(validator.isValid("v1=" + v1, REQUEST_ID, DATA_ID)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenXSignatureIsMissingV1Part() {
        assertThat(validator.isValid("ts=" + TS, REQUEST_ID, DATA_ID)).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenSecretDoesNotMatch() {
        String v1 = computeHmac("other-secret", DATA_ID, REQUEST_ID, TS);
        String xSignature = "ts=" + TS + ",v1=" + v1;

        assertThat(validator.isValid(xSignature, REQUEST_ID, DATA_ID)).isFalse();
    }

    private static String computeHmac(String secret, String dataId, String requestId, String ts) {
        try {
            String manifest = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
