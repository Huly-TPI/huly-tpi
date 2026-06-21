package com.huly.backend.infrastructure.adapter.pushNotification;
import com.huly.backend.domain.model.PushSubscription;
import lombok.RequiredArgsConstructor;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushNotificationAdapter {

            private final PushService pushService;
            public void send(PushSubscription sub, String payload) throws Exception {
                Notification notification = new Notification(sub.getEndpoint(), sub.getP256dh(), sub.getAuth(),
                payload.getBytes(StandardCharsets.UTF_8));
                pushService.send(notification, Encoding.AES128GCM);
            
            }
}
