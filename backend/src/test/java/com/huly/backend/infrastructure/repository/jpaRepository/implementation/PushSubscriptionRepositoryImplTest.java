package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.PushSubscription;
import com.huly.backend.infrastructure.repository.entity.PushSubscriptionEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPushSubscriptionJpaRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionRepositoryImplTest {

    private static final String ENDPOINT = "https://fcm.example.com/abc";

    @Mock
    private IPushSubscriptionJpaRepository jpaRepository;

    @InjectMocks
    private PushSubscriptionRepositoryImpl repositoryImpl;

    @Test
    @DisplayName("Mapea el dominio a entidad antes de persistir con save")
    void saveShouldMapDomainToEntityBeforePersisting() {
        givenSaved(savedEntityWithoutId());

        save(domainSubscription());

        thenPersistedEntityMatches();
    }

    @Test
    @DisplayName("Mapea la entidad guardada a dominio al guardar")
    void saveShouldReturnMappedDomain() {
        givenSaved(savedEntityWithId());

        PushSubscription result = save(domainSubscription());

        thenSubscriptionMatches(result);
    }

    @Test
    @DisplayName("Delega el borrado por endpoint al repositorio JPA")
    void deleteByEndpointShouldDelegateToJpaRepository() {
        deleteByEndpoint(ENDPOINT);

        thenDeletedByEndpoint(ENDPOINT);
    }

    @Test
    @DisplayName("Delega la existencia por endpoint al repositorio JPA")
    void existsByEndpointShouldDelegateToJpaRepository() {
        givenExists(ENDPOINT, true);

        boolean result = existsByEndpoint(ENDPOINT);

        thenTrue(result);
    }

    @Test
    @DisplayName("Mapea todas las suscripciones a dominio")
    void findAllShouldReturnMappedDomainList() {
        givenAll(subscriptionEntity());

        List<PushSubscription> result = findAll();

        thenSubscriptionListMatches(result);
    }

    @Test
    @DisplayName("Devuelve la suscripción por usuario cuando existe")
    void findByUserIdShouldReturnMappedDomainWhenExists() {
        givenByUserId(5L, subscriptionEntity());

        Optional<PushSubscription> result = findByUserId(5L);

        thenSubscriptionPresent(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar la suscripción por usuario cuando no existe")
    void findByUserIdShouldReturnEmptyWhenNotFound() {
        givenByUserId(99L, null);

        Optional<PushSubscription> result = findByUserId(99L);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Mapea las suscripciones por hora de notificación")
    void findByNotificationHourShouldReturnMappedDomainList() {
        givenByNotificationHour(20, subscriptionEntityWithHour());

        List<PushSubscription> result = findByNotificationHour(20);

        thenHourListMatches(result);
    }

    @Test
    @DisplayName("Delega la actualización de la hora de notificación por usuario")
    void updateNotificationHourByUserIdShouldDelegateToJpaRepository() {
        updateNotificationHour(5L, 18);

        thenUpdatedNotificationHour(5L, 18);
    }

    // --- arrange ---
    private void givenSaved(PushSubscriptionEntity entity) {
        when(jpaRepository.save(any())).thenReturn(entity);
    }

    private void givenExists(String endpoint, boolean exists) {
        when(jpaRepository.existsByEndpoint(endpoint)).thenReturn(exists);
    }

    private void givenAll(PushSubscriptionEntity entity) {
        when(jpaRepository.findAll()).thenReturn(List.of(entity));
    }

    private void givenByUserId(Long userId, PushSubscriptionEntity entity) {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenByNotificationHour(int hour, PushSubscriptionEntity entity) {
        when(jpaRepository.findByNotificationHour(hour)).thenReturn(List.of(entity));
    }

    private PushSubscription domainSubscription() {
        return PushSubscription.builder()
                .userId(1L).endpoint(ENDPOINT)
                .p256dh("key123").auth("auth123").build();
    }

    private PushSubscriptionEntity savedEntityWithoutId() {
        return PushSubscriptionEntity.builder()
                .userId(1L).endpoint(ENDPOINT)
                .p256dh("key123").auth("auth123").build();
    }

    private PushSubscriptionEntity savedEntityWithId() {
        return PushSubscriptionEntity.builder()
                .id(10L).userId(1L).endpoint(ENDPOINT)
                .p256dh("key123").auth("auth123").build();
    }

    private PushSubscriptionEntity subscriptionEntity() {
        return PushSubscriptionEntity.builder()
                .id(1L).userId(5L).endpoint(ENDPOINT)
                .p256dh("key").auth("auth").build();
    }

    private PushSubscriptionEntity subscriptionEntityWithHour() {
        return PushSubscriptionEntity.builder()
                .id(1L).userId(5L).endpoint(ENDPOINT)
                .p256dh("key").auth("auth").notificationHour(20).build();
    }

    // --- act ---
    private PushSubscription save(PushSubscription domain) {
        return repositoryImpl.save(domain);
    }

    private void deleteByEndpoint(String endpoint) {
        repositoryImpl.deleteByEndpoint(endpoint);
    }

    private boolean existsByEndpoint(String endpoint) {
        return repositoryImpl.existsByEndpoint(endpoint);
    }

    private List<PushSubscription> findAll() {
        return repositoryImpl.findAll();
    }

    private Optional<PushSubscription> findByUserId(Long userId) {
        return repositoryImpl.findByUserId(userId);
    }

    private List<PushSubscription> findByNotificationHour(int hour) {
        return repositoryImpl.findByNotificationHour(hour);
    }

    private void updateNotificationHour(Long userId, int hour) {
        repositoryImpl.updateNotificationHourByUserId(userId, hour);
    }

    // --- assert ---
    private void thenPersistedEntityMatches() {
        ArgumentCaptor<PushSubscriptionEntity> captor = ArgumentCaptor.forClass(PushSubscriptionEntity.class);
        verify(jpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getEndpoint()).isEqualTo(ENDPOINT);
        assertThat(captor.getValue().getP256dh()).isEqualTo("key123");
        assertThat(captor.getValue().getAuth()).isEqualTo("auth123");
    }

    private void thenSubscriptionMatches(PushSubscription result) {
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getEndpoint()).isEqualTo(ENDPOINT);
        assertThat(result.getP256dh()).isEqualTo("key123");
        assertThat(result.getAuth()).isEqualTo("auth123");
    }

    private void thenDeletedByEndpoint(String endpoint) {
        verify(jpaRepository).deleteByEndpoint(endpoint);
    }

    private void thenTrue(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenSubscriptionListMatches(List<PushSubscription> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getUserId()).isEqualTo(5L);
        assertThat(result.get(0).getEndpoint()).isEqualTo(ENDPOINT);
        assertThat(result.get(0).getP256dh()).isEqualTo("key");
        assertThat(result.get(0).getAuth()).isEqualTo("auth");
    }

    private void thenSubscriptionPresent(Optional<PushSubscription> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUserId()).isEqualTo(5L);
        assertThat(result.get().getEndpoint()).isEqualTo(ENDPOINT);
        assertThat(result.get().getP256dh()).isEqualTo("key");
        assertThat(result.get().getAuth()).isEqualTo("auth");
    }

    private void thenEmpty(Optional<PushSubscription> result) {
        assertThat(result).isEmpty();
    }

    private void thenHourListMatches(List<PushSubscription> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNotificationHour()).isEqualTo(20);
    }

    private void thenUpdatedNotificationHour(Long userId, int hour) {
        verify(jpaRepository).updateNotificationHourByUserId(userId, hour);
    }
}
