package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.repository.PushSubscriptionRepository;
import com.huly.backend.domain.useCase.pushNotification.DeletePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.SavePushSubscriptionUseCase;
import com.huly.backend.domain.useCase.pushNotification.GetPushSubscriptionStatusUseCase;
import com.huly.backend.domain.useCase.pushNotification.UnsubscribeFromEmailsUseCase;
import com.huly.backend.domain.repository.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PushNotificationUseCaseConfig {

    @Bean
    public SavePushSubscriptionUseCase savePushSubscriptionUseCase(
            PushSubscriptionRepository pushNotificationRepository) {
        return new SavePushSubscriptionUseCase(pushNotificationRepository);
    }

    @Bean
    public DeletePushSubscriptionUseCase deletePushSubscriptionUseCase(
            PushSubscriptionRepository pushNotificationRepository) {
        return new DeletePushSubscriptionUseCase(pushNotificationRepository);
    }

    @Bean
    public GetPushSubscriptionStatusUseCase getPushSubscriptionStatusUseCase(
            PushSubscriptionRepository pushNotificationRepository) {
        return new GetPushSubscriptionStatusUseCase(pushNotificationRepository);
    }

    @Bean
    public UnsubscribeFromEmailsUseCase unsubscribeFromEmailsUseCase(UserRepository userRepository) {
        return new UnsubscribeFromEmailsUseCase(userRepository);
    }
}
