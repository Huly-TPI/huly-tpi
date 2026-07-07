package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.infrastructure.repository.entity.PaymentEventEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPaymentEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventRepositoryImplTest {

    private static final Long EVENT_ID = 1L;
    private static final Long USER_ID = 10L;
    private static final Long MP_PAYMENT_ID = 99L;
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Mock private IPaymentEventJpaRepository jpaRepository;
    @InjectMocks private PaymentEventRepositoryImpl repository;

    @Test
    @DisplayName("Mapea el dominio a entidad y de vuelta al guardar")
    void saveShouldMapDomainToEntityAndBack() {
        givenSaved(entity());

        PaymentEvent result = save(domainEvent());

        thenPersistedEntityMatches();
        thenResultMatchesEntity(result);
    }

    @Test
    @DisplayName("Devuelve el evento mapeado al buscar por mpPaymentId cuando existe")
    void findByMpPaymentIdShouldReturnMappedEventWhenFound() {
        givenEventByMpPaymentId(entity());

        Optional<PaymentEvent> result = findByMpPaymentId();

        thenEventFoundByMpPaymentId(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por mpPaymentId cuando no existe")
    void findByMpPaymentIdShouldReturnEmptyWhenNotFound() {
        givenEventByMpPaymentId(null);

        Optional<PaymentEvent> result = findByMpPaymentId();

        thenEmpty(result);
    }

    @Test
    @DisplayName("Devuelve el evento mapeado al buscar por referencia externa cuando existe")
    void findByExternalReferenceShouldReturnMappedEventWhenFound() {
        givenEventByExternalReference("ext-ref", entity());

        Optional<PaymentEvent> result = findByExternalReference("ext-ref");

        thenEventFoundByExternalReference(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar por referencia externa cuando no existe")
    void findByExternalReferenceShouldReturnEmptyWhenNotFound() {
        givenEventByExternalReference("missing", null);

        Optional<PaymentEvent> result = findByExternalReference("missing");

        thenEmpty(result);
    }

    @Test
    @DisplayName("Actualiza y guarda el estado cuando el evento existe")
    void updateStatusShouldUpdateAndSaveWhenEventExists() {
        givenEventById(entity());

        updateStatus(PaymentStatus.FAILED, MP_PAYMENT_ID, "cc_rejected");

        thenStatusUpdatedAndSaved();
    }

    @Test
    @DisplayName("No hace nada al actualizar el estado cuando el evento no existe")
    void updateStatusShouldDoNothingWhenEventNotFound() {
        givenEventById(null);

        updateStatus(PaymentStatus.FAILED, MP_PAYMENT_ID, "err");

        thenNothingSaved();
    }

    @Test
    @DisplayName("Devuelve true al aprobar cuando se actualizó una fila")
    void approveIfPendingShouldReturnTrueWhenRowUpdated() {
        givenApproveResult(1);

        boolean result = approveIfPending();

        thenTrue(result);
    }

    @Test
    @DisplayName("Devuelve false al aprobar cuando no se actualizó ninguna fila")
    void approveIfPendingShouldReturnFalseWhenNoRowUpdated() {
        givenApproveResult(0);

        boolean result = approveIfPending();

        thenFalse(result);
    }

    @Test
    @DisplayName("Devuelve la lista mapeada al buscar por usuario")
    void findByUserIdShouldReturnMappedList() {
        givenEventsByUserId(entity());

        List<PaymentEvent> result = findByUserId();

        thenMappedListByUserId(result);
    }

    @Test
    @DisplayName("Devuelve la lista mapeada al buscar por usuario y estado")
    void findByUserIdAndStatusShouldReturnMappedList() {
        givenEventsByUserIdAndStatus(PaymentStatus.APPROVED, entity());

        List<PaymentEvent> result = findByUserIdAndStatus(PaymentStatus.APPROVED);

        thenMappedListByUserIdAndStatus(result, PaymentStatus.APPROVED);
    }

    // --- arrange ---
    private void givenSaved(PaymentEventEntity entity) {
        when(jpaRepository.save(any(PaymentEventEntity.class))).thenReturn(entity);
    }

    private void givenEventByMpPaymentId(PaymentEventEntity entity) {
        when(jpaRepository.findByMpPaymentId(MP_PAYMENT_ID)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenEventByExternalReference(String reference, PaymentEventEntity entity) {
        when(jpaRepository.findByExternalReference(reference)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenEventById(PaymentEventEntity entity) {
        when(jpaRepository.findById(EVENT_ID)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenApproveResult(int updated) {
        when(jpaRepository.approveIfNotApproved(eq(EVENT_ID), eq(MP_PAYMENT_ID), any(Instant.class),
                eq(PaymentStatus.APPROVED), eq(PaymentStatus.PENDING))).thenReturn(updated);
    }

    private void givenEventsByUserId(PaymentEventEntity... entities) {
        when(jpaRepository.findByUserId(USER_ID)).thenReturn(List.of(entities));
    }

    private void givenEventsByUserIdAndStatus(PaymentStatus status, PaymentEventEntity... entities) {
        when(jpaRepository.findByUserIdAndStatus(USER_ID, status)).thenReturn(List.of(entities));
    }

    private PaymentEvent domainEvent() {
        return PaymentEvent.builder()
                .id(EVENT_ID).userId(USER_ID).productId(2L)
                .storeItemId(3L)
                .externalReference("ext-ref").mpPreferenceId("pref-123").mpPaymentId(MP_PAYMENT_ID)
                .status(PaymentStatus.PENDING).coinsAmount(500)
                .productType(ProductType.PLAN).errorDetail("none")
                .createdAt(CREATED_AT).updatedAt(CREATED_AT)
                .build();
    }

    private PaymentEventEntity entity() {
        return PaymentEventEntity.builder()
                .id(EVENT_ID).userId(USER_ID).productId(2L)
                .externalReference("ext-ref").mpPreferenceId("pref-123").mpPaymentId(MP_PAYMENT_ID)
                .status(PaymentStatus.PENDING).coinsAmount(500)
                .productType(ProductType.COIN_PACK).errorDetail(null)
                .createdAt(CREATED_AT).updatedAt(CREATED_AT)
                .build();
    }

    // --- act ---
    private PaymentEvent save(PaymentEvent domain) {
        return repository.save(domain);
    }

    private Optional<PaymentEvent> findByMpPaymentId() {
        return repository.findByMpPaymentId(MP_PAYMENT_ID);
    }

    private Optional<PaymentEvent> findByExternalReference(String reference) {
        return repository.findByExternalReference(reference);
    }

    private void updateStatus(PaymentStatus status, Long mpPaymentId, String errorDetail) {
        repository.updateStatus(EVENT_ID, status, mpPaymentId, errorDetail);
    }

    private boolean approveIfPending() {
        return repository.approveIfPending(EVENT_ID, MP_PAYMENT_ID);
    }

    private List<PaymentEvent> findByUserId() {
        return repository.findByUserId(USER_ID);
    }

    private List<PaymentEvent> findByUserIdAndStatus(PaymentStatus status) {
        return repository.findByUserIdAndStatus(USER_ID, status);
    }

    // --- assert ---
    private void thenPersistedEntityMatches() {
        ArgumentCaptor<PaymentEventEntity> captor = ArgumentCaptor.forClass(PaymentEventEntity.class);
        verify(jpaRepository).save(captor.capture());
        PaymentEventEntity persisted = captor.getValue();
        assertThat(persisted.getUserId()).isEqualTo(10L);
        assertThat(persisted.getProductId()).isEqualTo(2L);
        assertThat(persisted.getStoreItemId()).isEqualTo(3L);
        assertThat(persisted.getExternalReference()).isEqualTo("ext-ref");
        assertThat(persisted.getMpPreferenceId()).isEqualTo("pref-123");
        assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(persisted.getCoinsAmount()).isEqualTo(500);
        assertThat(persisted.getProductType()).isEqualTo(ProductType.PLAN);
        assertThat(persisted.getErrorDetail()).isEqualTo("none");
    }

    private void thenResultMatchesEntity(PaymentEvent result) {
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getCoinsAmount()).isEqualTo(500);
    }

    private void thenEventFoundByMpPaymentId(Optional<PaymentEvent> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getMpPaymentId()).isEqualTo(99L);
    }

    private void thenEventFoundByExternalReference(Optional<PaymentEvent> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getExternalReference()).isEqualTo("ext-ref");
    }

    private void thenEmpty(Optional<PaymentEvent> result) {
        assertThat(result).isEmpty();
    }

    private void thenStatusUpdatedAndSaved() {
        ArgumentCaptor<PaymentEventEntity> captor = ArgumentCaptor.forClass(PaymentEventEntity.class);
        verify(jpaRepository).save(captor.capture());
        PaymentEventEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(saved.getMpPaymentId()).isEqualTo(99L);
        assertThat(saved.getErrorDetail()).isEqualTo("cc_rejected");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    private void thenNothingSaved() {
        verify(jpaRepository, never()).save(any());
    }

    private void thenTrue(boolean result) {
        assertThat(result).isTrue();
    }

    private void thenFalse(boolean result) {
        assertThat(result).isFalse();
    }

    private void thenMappedListByUserId(List<PaymentEvent> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
        verify(jpaRepository).findByUserId(10L);
    }

    private void thenMappedListByUserIdAndStatus(List<PaymentEvent> result, PaymentStatus status) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
        verify(jpaRepository).findByUserIdAndStatus(10L, status);
    }
}
