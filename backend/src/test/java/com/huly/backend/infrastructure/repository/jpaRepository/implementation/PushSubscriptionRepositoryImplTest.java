package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.infrastructure.repository.entity.PushSubscriptionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPushSubscriptionJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionRepositoryImplTest {

    @Mock
    private IPushSubscriptionJpaRepository jpaRepository;

    @InjectMocks
    private PushSubscriptionRepositoryImpl repositoryImpl;

    @Test
    void save_shouldMapDomainToEntityBeforePersisting() {
        PushSubscription domain = PushSubscription.builder()
                .userId(1L).endpoint("https://fcm.example.com/abc")
                .p256dh("key123").auth("auth123").build();

        PushSubscriptionEntity entity = PushSubscriptionEntity.builder()
                .userId(1L).endpoint("https://fcm.example.com/abc")
                .p256dh("key123").auth("auth123").build();

        when(jpaRepository.save(any())).thenReturn(entity);

        ArgumentCaptor<PushSubscriptionEntity> captor = ArgumentCaptor.forClass(PushSubscriptionEntity.class);

        repositoryImpl.save(domain);

        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getEndpoint()).isEqualTo("https://fcm.example.com/abc");
        assertThat(captor.getValue().getP256dh()).isEqualTo("key123");
        assertThat(captor.getValue().getAuth()).isEqualTo("auth123");
    }

    @Test
    void save_shouldReturnMappedDomain() {
        PushSubscription domain = PushSubscription.builder()
                .userId(1L).endpoint("https://fcm.example.com/abc")
                .p256dh("key123").auth("auth123").build();

        PushSubscriptionEntity returned = PushSubscriptionEntity.builder()
                .id(10L).userId(1L).endpoint("https://fcm.example.com/abc")
                .p256dh("key123").auth("auth123").build();
        when(jpaRepository.save(any())).thenReturn(returned);

        PushSubscription result = repositoryImpl.save(domain);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEndpoint()).isEqualTo("https://fcm.example.com/abc");
        assertThat(result.getP256dh()).isEqualTo("key123");
        assertThat(result.getAuth()).isEqualTo("auth123");
    }

    @Test
    void deleteByEndpoint_shouldDelegateToJpaRepository() {
        repositoryImpl.deleteByEndpoint("https://fcm.example.com/abc");
        verify(jpaRepository).deleteByEndpoint("https://fcm.example.com/abc");
    }

    @Test
    void existsByEndpoint_shouldDelegateToJpaRepository() {
        when(jpaRepository.existsByEndpoint("https://fcm.example.com/abc")).thenReturn(true);
        assertThat(repositoryImpl.existsByEndpoint("https://fcm.example.com/abc")).isTrue();
    }

    @Test
    void findAll_shouldReturnMappedDomainList() {
        PushSubscriptionEntity entity = PushSubscriptionEntity.builder()
                .id(1L).userId(5L).endpoint("https://fcm.example.com/abc")
                .p256dh("key").auth("auth").build();
        when(jpaRepository.findAll()).thenReturn(List.of(entity));

        List<PushSubscription> result = repositoryImpl.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getUserId()).isEqualTo(5L);
        assertThat(result.get(0).getEndpoint()).isEqualTo("https://fcm.example.com/abc");
        assertThat(result.get(0).getP256dh()).isEqualTo("key");
        assertThat(result.get(0).getAuth()).isEqualTo("auth");
    }

    @Test
    void findByUserId_shouldReturnMappedDomain_whenExists() {
        PushSubscriptionEntity entity = PushSubscriptionEntity.builder()
                .id(1L).userId(5L).endpoint("https://fcm.example.com/abc")
                .p256dh("key").auth("auth").build();
        when(jpaRepository.findByUserId(5L)).thenReturn(Optional.of(entity));

        Optional<PushSubscription> result = repositoryImpl.findByUserId(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUserId()).isEqualTo(5L);
        assertThat(result.get().getEndpoint()).isEqualTo("https://fcm.example.com/abc");
        assertThat(result.get().getP256dh()).isEqualTo("key");
        assertThat(result.get().getAuth()).isEqualTo("auth");
    }

    @Test
    void findByUserId_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findByUserId(99L)).thenReturn(Optional.empty());

        Optional<PushSubscription> result = repositoryImpl.findByUserId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByNotificationHour_shouldReturnMappedDomainList() {
        PushSubscriptionEntity entity = PushSubscriptionEntity.builder()
                .id(1L).userId(5L).endpoint("https://fcm.example.com/abc")
                .p256dh("key").auth("auth").notificationHour(20).build();
        when(jpaRepository.findByNotificationHour(20)).thenReturn(List.of(entity));

        List<PushSubscription> result = repositoryImpl.findByNotificationHour(20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotificationHour()).isEqualTo(20);
    }

    @Test
    void updateNotificationHourByUserId_shouldDelegateToJpaRepository() {
        repositoryImpl.updateNotificationHourByUserId(5L, 18);
        verify(jpaRepository).updateNotificationHourByUserId(5L, 18);
    }
}
