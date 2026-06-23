package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class SavePushSubscriptionUseCase {
    
        private final PushSubscriptionRepository pushSubscriptionRepository;
    
       public PushSubscription execute(Long userId, String endpoint, String p256dh, String auth) {
           if(pushSubscriptionRepository.existsByEndpoint(endpoint)) {
            return null;
           }

           PushSubscription pushSubscription = PushSubscription.builder()
                   .userId(userId)
                   .endpoint(endpoint)
                   .p256dh(p256dh)
                   .auth(auth)
                   .createdAt(LocalDateTime.now())
                   .build();
           return pushSubscriptionRepository.save(pushSubscription);
       }
}
