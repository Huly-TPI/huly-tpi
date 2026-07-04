package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import com.huly.backend.infrastructure.adapter.pushNotification.PushNotificationAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PushNotificationSchedulerTest {

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Mock
    private PushNotificationAdapter pushNotificationAdapter;

    @InjectMocks
    private PushNotificationScheduler scheduler;

    @Test
    void sendNotificationsForHour_shouldSendPushToEachSubscription() throws Exception {
        PushSubscription sub1 = PushSubscription.builder().id(1L).userId(10L).endpoint("https://fcm.example.com/1")
                .p256dh("key1").auth("auth1").build();
        PushSubscription sub2 = PushSubscription.builder().id(2L).userId(20L).endpoint("https://fcm.example.com/2")
                .p256dh("key2").auth("auth2").build();
        when(pushSubscriptionRepository.findByNotificationHour(9)).thenReturn(List.of(sub1, sub2));
        scheduler.sendNotificationsForHour(9);
        verify(pushNotificationAdapter, times(2)).send(any(), any());
    }

    @Test
    void sendNotificationsForHour_shouldContinue_whenOnePushFails() throws Exception {
        PushSubscription sub1 = PushSubscription.builder().id(1L).userId(10L).endpoint("https://fcm.example.com/1")
                .p256dh("key1").auth("auth1").build();
        PushSubscription sub2 = PushSubscription.builder().id(2L).userId(20L).endpoint("https://fcm.example.com/2")
                .p256dh("key2").auth("auth2").build();
        when(pushSubscriptionRepository.findByNotificationHour(9)).thenReturn(List.of(sub1, sub2));
        doThrow(new RuntimeException("push failed")).when(pushNotificationAdapter).send(any(), any());
        scheduler.sendNotificationsForHour(9);
        verify(pushNotificationAdapter, times(2)).send(any(), any());
    }

    @Test
    void sendNotificationsForHour_shouldDoNothing_whenNoSubscriptions() throws Exception {
        when(pushSubscriptionRepository.findByNotificationHour(9)).thenReturn(List.of());
        scheduler.sendNotificationsForHour(9);
        verify(pushNotificationAdapter, never()).send(any(), any());
    }
}
