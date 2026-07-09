package com.huly.backend.domain.useCase.admin;

import com.huly.backend.domain.model.payment.PaymentEvent;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.payment.PaymentEventRepository;
import com.huly.backend.domain.repository.payment.ProductRepository;
import com.huly.backend.domain.repository.user.UserRepository;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsRequest;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsResponse;
import com.huly.backend.domain.useCase.admin.userFinancials.GetUserFinancialsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserFinancialsUseCaseTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentEventRepository paymentEventRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetUserFinancialsUseCase useCase;

    @Test
    @DisplayName("Lanza excepción cuando el usuario no existe")
    void executeShouldThrowWhenUserNotFound() {
        // --- arrange ---
        givenUserNotFound();

        // --- assert ---
        thenFinancialsThrowsUserNotFound();
    }

    @Test
    @DisplayName("Arma la respuesta a partir de los objetos de dominio")
    void executeShouldBuildResponseFromDomainObjects() {
        // --- arrange ---
        givenUserExists();
        givenPayments(approvedPayment(200L, 100L));
        givenApprovedPayments(approvedPayment(200L, 100L));
        givenProductLookup(List.of(100L), premiumPlan());

        // --- act ---
        GetUserFinancialsResponse response = financials();

        // --- assert ---
        thenSinglePremiumPayment(response);
    }

    @Test
    @DisplayName("Lanza excepción cuando falta el producto de un pago")
    void executeShouldThrowWhenProductIsMissing() {
        // --- arrange ---
        givenUserExists();
        givenPayments(approvedPayment(200L, 999L));
        givenProductLookup(List.of(999L));

        // --- assert ---
        thenFinancialsThrowsMissingProduct("Missing product for paymentEventId=200, productId=999");
    }

    @Test
    @DisplayName("Lanza excepción cuando el pago no tiene productId")
    void executeShouldThrowWhenProductIdIsNull() {
        // --- arrange ---
        givenUserExists();
        givenPayments(payment(200L, null, null, null));
        givenProductLookup(List.of());

        // --- assert ---
        thenFinancialsThrowsMissingProduct("Missing product for paymentEventId=200, productId=null");
    }

    @Test
    @DisplayName("Deduplica los productIds y suma las ganancias de pagos repetidos")
    void executeShouldHandleDuplicateProductIds() {
        // --- arrange ---
        givenUserExists();
        givenPayments(approvedPayment(200L, 100L), approvedPayment(201L, 100L));
        givenApprovedPayments(approvedPayment(200L, 100L), approvedPayment(201L, 100L));
        givenProductLookup(List.of(100L), premiumPlan(), premiumPlan());

        // --- act ---
        GetUserFinancialsResponse response = financials();

        // --- assert ---
        thenTwoPaymentsTotalling(response, "39.98");
    }

    @Test
    @DisplayName("Devuelve respuesta vacía y ganancias en cero cuando no hay pagos")
    void executeShouldReturnEmptyWhenNoPayments() {
        // --- arrange ---
        givenUserExists();
        givenNoPayments();
        givenApprovedPayments();
        givenProductLookup(List.of());

        // --- act ---
        GetUserFinancialsResponse response = financials();

        // --- assert ---
        thenEmptyFinancials(response);
    }

    @Test
    @DisplayName("Lanza excepción cuando el pago no tiene estado")
    void executeShouldThrowWhenPaymentStatusIsNull() {
        // --- arrange ---
        givenUserExists();
        givenPayments(payment(200L, 100L, null, ProductType.PLAN));
        givenProductLookup(List.of(100L), premiumPlan());

        // --- assert ---
        thenFinancialsThrowsNullPointer("PaymentEvent status is required");
    }

    @Test
    @DisplayName("Lanza excepción cuando el pago no tiene tipo de producto")
    void executeShouldThrowWhenProductTypeIsNull() {
        // --- arrange ---
        givenUserExists();
        givenPayments(payment(200L, 100L, PaymentStatus.APPROVED, null));
        givenProductLookup(List.of(100L), premiumPlan());

        // --- assert ---
        thenFinancialsThrowsNullPointer("PaymentEvent productType is required");
    }

    @Test
    @DisplayName("Lanza excepción cuando el producto aprobado no tiene precio")
    void executeShouldThrowWhenApprovedProductPriceIsNull() {
        // --- arrange ---
        givenUserExists();
        givenPayments(approvedPayment(200L, 100L));
        givenApprovedPayments(approvedPayment(200L, 100L));
        givenProductLookup(List.of(100L), planWithoutPrice());

        // --- assert ---
        thenFinancialsThrowsNullPointer("Product price is required");
    }

    // --- arrange ---

    private void givenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(AppUser.builder().id(USER_ID).build()));
    }

    private void givenUserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
    }

    private void givenPayments(PaymentEvent... payments) {
        when(paymentEventRepository.findByUserId(USER_ID)).thenReturn(List.of(payments));
    }

    private void givenNoPayments() {
        when(paymentEventRepository.findByUserId(USER_ID)).thenReturn(List.of());
    }

    private void givenApprovedPayments(PaymentEvent... payments) {
        when(paymentEventRepository.findByUserIdAndStatus(USER_ID, PaymentStatus.APPROVED)).thenReturn(List.of(payments));
    }

    private void givenProductLookup(List<Long> expectedIds, Product... products) {
        when(productRepository.findByIds(expectedIds)).thenReturn(List.of(products));
    }

    private PaymentEvent approvedPayment(Long id, Long productId) {
        return payment(id, productId, PaymentStatus.APPROVED, ProductType.PLAN);
    }

    private PaymentEvent payment(Long id, Long productId, PaymentStatus status, ProductType productType) {
        return PaymentEvent.builder()
                .id(id)
                .userId(USER_ID)
                .productId(productId)
                .status(status)
                .productType(productType)
                .createdAt(Instant.now())
                .build();
    }

    private Product premiumPlan() {
        return Product.builder()
                .id(100L)
                .name("Premium Plan")
                .price(new BigDecimal("19.99"))
                .type(ProductType.PLAN)
                .build();
    }

    private Product planWithoutPrice() {
        return Product.builder()
                .id(100L)
                .name("Premium Plan")
                .price(null)
                .type(ProductType.PLAN)
                .build();
    }

    // --- act ---

    private GetUserFinancialsResponse financials() {
        return useCase.execute(new GetUserFinancialsRequest(USER_ID));
    }

    // --- assert ---

    private void thenSinglePremiumPayment(GetUserFinancialsResponse response) {
        assertThat(response.totalEarnings()).isEqualByComparingTo("19.99");
        assertThat(response.paymentEvents()).singleElement().satisfies(item -> {
            assertThat(item.productId()).isEqualTo(100L);
            assertThat(item.productName()).isEqualTo("Premium Plan");
            assertThat(item.productPrice()).isEqualByComparingTo("19.99");
            assertThat(item.status()).isEqualTo("APPROVED");
        });
    }

    private void thenTwoPaymentsTotalling(GetUserFinancialsResponse response, String expectedTotal) {
        assertThat(response.totalEarnings()).isEqualByComparingTo(expectedTotal);
        assertThat(response.paymentEvents()).hasSize(2);
    }

    private void thenEmptyFinancials(GetUserFinancialsResponse response) {
        assertThat(response.paymentEvents()).isEmpty();
        assertThat(response.totalEarnings()).isEqualByComparingTo("0");
    }

    private void thenFinancialsThrowsUserNotFound() {
        assertThatThrownBy(this::financials)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    private void thenFinancialsThrowsMissingProduct(String message) {
        assertThatThrownBy(this::financials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(message);
    }

    private void thenFinancialsThrowsNullPointer(String message) {
        assertThatThrownBy(this::financials)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(message);
    }
}
