package com.huly.backend.domain.useCase.payment;

import com.huly.backend.domain.exception.BusinessRuleException;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.port.MercadoPagoPort;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.repository.UserStoreItemRepository;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStoreItemPreferenceUseCaseTest {

    @Mock
    private StoreItemRepository storeItemRepository;
    @Mock
    private MercadoPagoPort mercadoPagoPort;
    @Mock
    private PaymentEventRepository paymentEventRepository;
    @Mock
    private UserStoreItemRepository userStoreItemRepository;

    private CreateStoreItemPreferenceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateStoreItemPreferenceUseCase(
                storeItemRepository, mercadoPagoPort, paymentEventRepository, userStoreItemRepository);
    }

    private StoreItem itemWithPrice() {
        return StoreItem.builder()
                .id(10L).name("Cuaderno rosa").description("Un cuaderno rosa")
                .category(ItemCategory.NOTEBOOK).assetKey("notebook-pink")
                .priceCoins(50).price(new BigDecimal("1000.00"))
                .build();
    }

    private StoreItem itemWithoutPrice() {
        return StoreItem.builder()
                .id(11L).name("Cuaderno celeste").description("Un cuaderno celeste")
                .category(ItemCategory.NOTEBOOK).assetKey("notebook-blue")
                .priceCoins(50).price(null)
                .build();
    }

    @Test
    void execute_shouldReturnPreferenceIdAndInitPoint_whenItemBuyableWithMoney() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(itemWithPrice()));
        when(userStoreItemRepository.isOwned(5L, 10L)).thenReturn(false);
        when(mercadoPagoPort.createPreference(any(), eq(5L), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentPreferenceResult result = useCase.execute(10L, 5L);

        assertThat(result.getId()).isEqualTo("pref-123");
        assertThat(result.getInitPoint()).isEqualTo("https://mp.com/checkout");
    }

    @Test
    void execute_shouldSaveEventAsStoreItemPurchase_pendingWithZeroCoins() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(itemWithPrice()));
        when(userStoreItemRepository.isOwned(5L, 10L)).thenReturn(false);
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-real-456", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(10L, 5L);

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentEventRepository).save(captor.capture());
        PaymentEvent saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(5L);
        assertThat(saved.getStoreItemId()).isEqualTo(10L);
        assertThat(saved.getProductId()).isNull();
        assertThat(saved.getProductType()).isEqualTo(ProductType.STORE_ITEM);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getCoinsAmount()).isEqualTo(0);
        assertThat(saved.getMpPreferenceId()).isEqualTo("pref-real-456");
        assertThat(saved.getExternalReference()).matches(
                "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void execute_shouldPassExternalReferenceToMpPort() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(itemWithPrice()));
        when(userStoreItemRepository.isOwned(5L, 10L)).thenReturn(false);
        when(mercadoPagoPort.createPreference(any(), any(), any()))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));
        when(paymentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(10L, 5L);

        ArgumentCaptor<String> ref = ArgumentCaptor.forClass(String.class);
        verify(mercadoPagoPort).createPreference(any(), eq(5L), ref.capture());
        assertThat(ref.getValue()).isNotBlank();
    }

    @Test
    void execute_shouldThrowResourceNotFound_whenItemDoesNotExist() {
        when(storeItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(mercadoPagoPort, never()).createPreference(any(), any(), any());
        verify(paymentEventRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowBusinessRule_whenItemHasNoMoneyPrice() {
        when(storeItemRepository.findById(11L)).thenReturn(Optional.of(itemWithoutPrice()));

        assertThatThrownBy(() -> useCase.execute(11L, 5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dinero");

        verify(mercadoPagoPort, never()).createPreference(any(), any(), any());
        verify(paymentEventRepository, never()).save(any());
    }

    @Test
    void execute_shouldThrowBusinessRule_whenAlreadyOwned() {
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(itemWithPrice()));
        when(userStoreItemRepository.isOwned(5L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(10L, 5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Ya tenés");

        verify(mercadoPagoPort, never()).createPreference(any(), any(), any());
        verify(paymentEventRepository, never()).save(any());
    }
}