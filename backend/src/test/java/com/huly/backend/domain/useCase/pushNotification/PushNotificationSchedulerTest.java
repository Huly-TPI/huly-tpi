package com.huly.backend.domain.useCase.pushNotification;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.domain.repository.PushSubscriptionRepository;
import com.huly.backend.infrastructure.adapter.pushNotification.PushNotificationAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationSchedulerTest {

    private static final int HOUR = 9;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Mock
    private PushNotificationAdapter pushNotificationAdapter;

    @InjectMocks
    private PushNotificationScheduler scheduler;

    @Test
    @DisplayName("Envía un push a cada suscripción de la hora")
    void sendNotificationsForHourShouldSendPushToEachSubscription() throws Exception {
        // --- arrange ---
        givenTwoSubscriptionsForHour();
        // --- act ---
        sendNotificationsForHour();
        // --- assert ---
        thenPushSentToEachSubscription();
    }

    @Test
    @DisplayName("Continúa con las demás suscripciones cuando un push falla")
    void sendNotificationsForHourShouldContinueWhenOnePushFails() throws Exception {
        // --- arrange ---
        givenTwoSubscriptionsForHour();
        givenSendAlwaysFails();
        // --- act ---
        sendNotificationsForHour();
        // --- assert ---
        thenPushSentToEachSubscription();
    }

    @Test
    @DisplayName("No hace nada cuando no hay suscripciones para la hora")
    void sendNotificationsForHourShouldDoNothingWhenNoSubscriptions() throws Exception {
        // --- arrange ---
        givenNoSubscriptionsForHour();
        // --- act ---
        sendNotificationsForHour();
        // --- assert ---
        thenNoPushSent();
    }

    @Test
    @DisplayName("Consulta las suscripciones por la hora actual")
    void sendDailyNotificationsShouldQueryByCurrentHour() {
        // --- arrange ---
        givenNoSubscriptionsForAnyHour();
        // --- act ---
        sendDailyNotifications();
        // --- assert ---
        thenQueriedByCurrentHour();
    }

    // --- arrange ---

    private PushSubscription subscription(long id, long userId, String endpoint, String p256dh, String auth) {
        return PushSubscription.builder()
                .id(id).userId(userId).endpoint(endpoint).p256dh(p256dh).auth(auth)
                .build();
    }

    private void givenTwoSubscriptionsForHour() {
        when(pushSubscriptionRepository.findByNotificationHour(HOUR)).thenReturn(List.of(
                subscription(1L, 10L, "https://fcm.example.com/1", "key1", "auth1"),
                subscription(2L, 20L, "https://fcm.example.com/2", "key2", "auth2")));
    }

    private void givenNoSubscriptionsForHour() {
        when(pushSubscriptionRepository.findByNotificationHour(HOUR)).thenReturn(List.of());
    }

    private void givenNoSubscriptionsForAnyHour() {
        when(pushSubscriptionRepository.findByNotificationHour(anyInt())).thenReturn(List.of());
    }

    private void givenSendAlwaysFails() throws Exception {
        doThrow(new RuntimeException("push failed")).when(pushNotificationAdapter).send(any(), any());
    }

    // --- act ---

    private void sendNotificationsForHour() {
        scheduler.sendNotificationsForHour(HOUR);
    }

    private void sendDailyNotifications() {
        scheduler.sendDailyNotifications();
    }

    // --- assert ---

    private void thenPushSentToEachSubscription() throws Exception {
        verify(pushNotificationAdapter, times(2)).send(any(), any());
    }

    private void thenNoPushSent() throws Exception {
        verify(pushNotificationAdapter, never()).send(any(), any());
    }

    private void thenQueriedByCurrentHour() {
        verify(pushSubscriptionRepository).findByNotificationHour(anyInt());
    }
}
