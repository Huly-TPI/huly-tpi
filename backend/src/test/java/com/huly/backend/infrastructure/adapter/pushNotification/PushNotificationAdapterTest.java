package com.huly.backend.infrastructure.adapter.pushNotification;
import com.huly.backend.domain.model.PushSubscription;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PushNotificationAdapterTest {

    private PushService pushService;
    private PushNotificationAdapter adapter;

    @BeforeEach
    void setUp() { 
        pushService = mock(PushService.class);
        adapter = new PushNotificationAdapter(pushService);
    }

    @Test 
    void send_invocaPushServiceConEncodingAes128() throws Exception {
        PushSubscription sub = PushSubscription.builder() 
            .endpoint("https://fcm.example.com/1")
            .p256dh("p256dh")
            .auth("auth")
            .build();
    
            try (MockedConstruction<Notification> ignored = mockConstruction(Notification.class)) {
                adapter.send(sub, "{\"title\":\"Hola\"}");
                verify(pushService).send(any(), eq(Encoding.AES128GCM));
            }
        }
}
