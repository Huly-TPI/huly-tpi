package com.huly.backend.domain.useCase.pushNotification;
import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import com.huly.backend.infrastructure.adapter.pushNotification.PushNotificationAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationScheduler {
      
        private final PushSubscriptionRepository pushSubscriptionRepository;
        private final PushNotificationAdapter pushNotificationAdapter;

        private static final String PAYLOAD = """
                    {"title":"Huly te espera","body":"¿Cómo te sentís hoy? Entrá y registrá tu estado emocional."}
                """;

        

        @Scheduled(cron = "0 0 9 * * *")
        public void sendDailyNotifications() {
                for(PushSubscription subscription : pushSubscriptionRepository.findAll()) {
                        try {
                                pushNotificationAdapter.send(subscription, PAYLOAD);
                        } catch (Exception e) {
                                log.error("No se pudo enviar push a endpoint {}: {}", subscription.getEndpoint(), e.getMessage());
                        }
                }
        }
}
