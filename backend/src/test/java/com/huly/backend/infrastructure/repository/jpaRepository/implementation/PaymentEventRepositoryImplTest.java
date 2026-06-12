package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.infrastructure.repository.entity.PaymentEventEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IPaymentEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventRepositoryImplTest {

    @Mock private IPaymentEventJpaRepository jpaRepository;
    @InjectMocks private PaymentEventRepositoryImpl repository;

    private PaymentEventEntity entity() {
        return PaymentEventEntity.builder()
                .id(1L).userId(10L).productId(2L)
                .externalReference("ext-ref").mpPreferenceId("pref-123").mpPaymentId(99L)
                .status(PaymentStatus.PENDING).coinsAmount(500)
                .productType(ProductType.COIN_PACK).errorDetail(null)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    @Test
    void save_shouldMapDomainToEntityAndBack() {
        Instant created = Instant.now();
        PaymentEvent domain = PaymentEvent.builder()
                .id(1L).userId(10L).productId(2L)
                .externalReference("ext-ref").mpPreferenceId("pref-123").mpPaymentId(99L)
                .status(PaymentStatus.PENDING).coinsAmount(500)
                .productType(ProductType.PLAN).errorDetail("none")
                .createdAt(created).updatedAt(created)
                .build();
        when(jpaRepository.save(any(PaymentEventEntity.class))).thenReturn(entity());

        PaymentEvent result = repository.save(domain);

        ArgumentCaptor<PaymentEventEntity> captor = ArgumentCaptor.forClass(PaymentEventEntity.class);
        verify(jpaRepository).save(captor.capture());
        PaymentEventEntity persisted = captor.getValue();
        assertThat(persisted.getUserId()).isEqualTo(10L);
        assertThat(persisted.getProductId()).isEqualTo(2L);
        assertThat(persisted.getExternalReference()).isEqualTo("ext-ref");
        assertThat(persisted.getMpPreferenceId()).isEqualTo("pref-123");
        assertThat(persisted.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(persisted.getCoinsAmount()).isEqualTo(500);
        assertThat(persisted.getProductType()).isEqualTo(ProductType.PLAN);
        assertThat(persisted.getErrorDetail()).isEqualTo("none");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getCoinsAmount()).isEqualTo(500);
    }

    @Test
    void findByMpPaymentId_shouldReturnMappedEvent_whenFound() {
        when(jpaRepository.findByMpPaymentId(99L)).thenReturn(Optional.of(entity()));

        Optional<PaymentEvent> result = repository.findByMpPaymentId(99L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getMpPaymentId()).isEqualTo(99L);
    }

    @Test
    void findByMpPaymentId_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findByMpPaymentId(99L)).thenReturn(Optional.empty());

        assertThat(repository.findByMpPaymentId(99L)).isEmpty();
    }

    @Test
    void findByExternalReference_shouldReturnMappedEvent_whenFound() {
        when(jpaRepository.findByExternalReference("ext-ref")).thenReturn(Optional.of(entity()));

        Optional<PaymentEvent> result = repository.findByExternalReference("ext-ref");

        assertThat(result).isPresent();
        assertThat(result.get().getExternalReference()).isEqualTo("ext-ref");
    }

    @Test
    void findByExternalReference_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findByExternalReference("missing")).thenReturn(Optional.empty());

        assertThat(repository.findByExternalReference("missing")).isEmpty();
    }

    @Test
    void updateStatus_shouldUpdateAndSave_whenEventExists() {
        PaymentEventEntity entity = entity();
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        repository.updateStatus(1L, PaymentStatus.FAILED, 99L, "cc_rejected");

        ArgumentCaptor<PaymentEventEntity> captor = ArgumentCaptor.forClass(PaymentEventEntity.class);
        verify(jpaRepository).save(captor.capture());
        PaymentEventEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(saved.getMpPaymentId()).isEqualTo(99L);
        assertThat(saved.getErrorDetail()).isEqualTo("cc_rejected");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateStatus_shouldDoNothing_whenEventNotFound() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.empty());

        repository.updateStatus(1L, PaymentStatus.FAILED, 99L, "err");

        verify(jpaRepository, never()).save(any());
    }

    @Test
    void approveIfPending_shouldReturnTrue_whenRowUpdated() {
        when(jpaRepository.approveIfNotApproved(eq(1L), eq(99L), any(Instant.class),
                eq(PaymentStatus.APPROVED), eq(PaymentStatus.PENDING))).thenReturn(1);

        assertThat(repository.approveIfPending(1L, 99L)).isTrue();
    }

    @Test
    void approveIfPending_shouldReturnFalse_whenNoRowUpdated() {
        when(jpaRepository.approveIfNotApproved(eq(1L), eq(99L), any(Instant.class),
                eq(PaymentStatus.APPROVED), eq(PaymentStatus.PENDING))).thenReturn(0);

        assertThat(repository.approveIfPending(1L, 99L)).isFalse();
    }
}
