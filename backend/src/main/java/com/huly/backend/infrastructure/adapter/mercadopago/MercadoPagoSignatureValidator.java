package com.huly.backend.infrastructure.adapter.mercadopago;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
public class MercadoPagoSignatureValidator {

    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;

    public boolean isValid(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || xRequestId == null || dataId == null) return false;

        String ts = extractPart(xSignature, "ts");
        String v1 = extractPart(xSignature, "v1");
        if (ts == null || v1 == null) return false;

        String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts;
        String computed = hmacSha256Hex(webhookSecret.trim(), manifest);
        log.info("MP signature check — secretLen={} manifest='{}' computed={} expected={}",
                webhookSecret.trim().length(), manifest, computed, v1);
        return computed.equals(v1);
    }

    private String extractPart(String xSignature, String key) {
        for (String part : xSignature.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].trim().equals(key)) return kv[1].trim();
        }
        return null;
    }

    private String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HmacSHA256 unavailable", e);
        }
    }
}
