package com.huly.backend.infrastructure.config.useCase;

import com.huly.backend.domain.mapper.pushNotification.DeletePushSubscriptionMapper;
import com.huly.backend.domain.mapper.pushNotification.GetPushSubscriptionStatusMapper;
import com.huly.backend.domain.mapper.pushNotification.SavePushSubscriptionMapper;
import com.huly.backend.domain.mapper.pushNotification.UnsubscribeFromEmailsMapper;
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
    public SavePushSubscriptionMapper savePushSubscriptionMapper() {
        return new SavePushSubscriptionMapper();
    }

    @Bean
    public DeletePushSubscriptionMapper deletePushSubscriptionMapper() {
        return new DeletePushSubscriptionMapper();
    }

    @Bean
    public GetPushSubscriptionStatusMapper getPushSubscriptionStatusMapper() {
        return new GetPushSubscriptionStatusMapper();
    }

    @Bean
    public UnsubscribeFromEmailsMapper unsubscribeFromEmailsMapper() {
        return new UnsubscribeFromEmailsMapper();
    }

    @Bean
    public SavePushSubscriptionUseCase savePushSubscriptionUseCase(
            PushSubscriptionRepository pushNotificationRepository,
            SavePushSubscriptionMapper savePushSubscriptionMapper) {
        return new SavePushSubscriptionUseCase(pushNotificationRepository, savePushSubscriptionMapper);
    }

    @Bean
    public DeletePushSubscriptionUseCase deletePushSubscriptionUseCase(
            PushSubscriptionRepository pushNotificationRepository,
            DeletePushSubscriptionMapper deletePushSubscriptionMapper) {
        return new DeletePushSubscriptionUseCase(pushNotificationRepository, deletePushSubscriptionMapper);
    }

    @Bean
    public GetPushSubscriptionStatusUseCase getPushSubscriptionStatusUseCase(
            PushSubscriptionRepository pushNotificationRepository,
            GetPushSubscriptionStatusMapper getPushSubscriptionStatusMapper) {
        return new GetPushSubscriptionStatusUseCase(pushNotificationRepository, getPushSubscriptionStatusMapper);
    }

    @Bean
    public UnsubscribeFromEmailsUseCase unsubscribeFromEmailsUseCase(
            UserRepository userRepository,
            UnsubscribeFromEmailsMapper unsubscribeFromEmailsMapper) {
        return new UnsubscribeFromEmailsUseCase(userRepository, unsubscribeFromEmailsMapper);
    }
}
