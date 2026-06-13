package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.PushSubscriptionRepository;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class PushNotificationUseCaseConfig {
    
    @Bean
    public SavePushSubscriptionUseCase savePushSubscriptionUseCase(PushSubscriptionRepository pushNotificationRepository) {
        return new SavePushSubscriptionUseCase(pushNotificationRepository);
    }

    @Bean
    public DeletePushSubscriptionUseCase deletePushSubscriptionUseCase(PushSubscriptionRepository pushNotificationRepository) {
        return new DeletePushSubscriptionUseCase(pushNotificationRepository);
    }
}
