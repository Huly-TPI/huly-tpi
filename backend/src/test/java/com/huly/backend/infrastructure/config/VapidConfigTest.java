package com.huly.backend.infrastructure.config;

import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class VapidConfigTest {

    // Par de claves VAPID P-256 reales (clave publica: punto EC descomprimido de 65 bytes;
    // clave privada: escalar de 32 bytes) para que PushService pueda parsearlas sin red ni
    // servicios externos. Tomadas de los recursos de test del proyecto.
    private static final String PUBLIC_KEY =
            "BBjV7ZlfsOq0YhBZJbvQTYL5VbcFd_VYbuMsXtgz9Y6vByra-qG2p_HfzyYT01balK5G_dmxrRgkD1g4QI7VQxI";
    private static final String PRIVATE_KEY = "cb7SVhA4h8Q0sGGXQNRrrAQbc0zA65vJKJzU4afLfGM";
    private static final String SUBJECT = "mailto:test@huly.com";

    private final VapidConfig config = new VapidConfig();

    @Test
    @DisplayName("Crea un PushService no nulo a partir de las claves VAPID configuradas")
    void pushServiceShouldReturnNonNullServiceWhenVapidKeysAreConfigured() throws Exception {
        // --- arrange ---
        givenConfiguredVapidKeys();

        // --- act ---
        PushService result = createPushService();

        // --- assert ---
        thenPushServiceIsNotNull(result);
    }

    // --- arrange ---

    private void givenConfiguredVapidKeys() {
        ReflectionTestUtils.setField(config, "publicKey", PUBLIC_KEY);
        ReflectionTestUtils.setField(config, "privateKey", PRIVATE_KEY);
        ReflectionTestUtils.setField(config, "subject", SUBJECT);
    }

    // --- act ---

    private PushService createPushService() throws Exception {
        return config.pushService();
    }

    // --- assert ---

    private void thenPushServiceIsNotNull(PushService result) {
        assertThat(result).isNotNull();
    }
}
