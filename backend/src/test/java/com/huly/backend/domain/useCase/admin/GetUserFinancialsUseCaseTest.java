package com.huly.backend.domain.useCase.admin.userFinancials;

import com.huly.backend.domain.dto.payment.PaymentEvent;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.PaymentStatus;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.repository.PaymentEventRepository;
import com.huly.backend.domain.repository.ProductRepository;
import com.huly.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserFinancialsUseCaseTest {

    private UserRepository userRepository;
    private PaymentEventRepository paymentEventRepository;
    private ProductRepository productRepository;
    private GetUserFinancialsUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        paymentEventRepository = mock(PaymentEventRepository.class);
        productRepository = mock(ProductRepository.class);
        useCase = new GetUserFinancialsUseCase(userRepository, paymentEventRepository, productRepository);
    }

    @Test
    void execute_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetUserFinancialsRequest(1L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuario no encontrado");
    }

    @Test
    void execute_shouldBuildUseCaseResponseFromDomainObjects() {
        Long userId = 1L;
        Product product = Product.builder()
                .id(100L)
                .name("Premium Plan")
                .price(new BigDecimal("19.99"))
                .type(ProductType.PLAN)
                .build();
        PaymentEvent payment = PaymentEvent.builder()
                .id(200L)
                .userId(userId)
                .productId(100L)
                .status(PaymentStatus.APPROVED)
                .productType(ProductType.PLAN)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(AppUser.builder().id(userId).build()));
        when(paymentEventRepository.findByUserId(userId)).thenReturn(List.of(payment));
        when(paymentEventRepository.findByUserIdAndStatus(userId, PaymentStatus.APPROVED)).thenReturn(List.of(payment));
        when(productRepository.findByIds(List.of(100L))).thenReturn(List.of(product));

        GetUserFinancialsResponse response = useCase.execute(new GetUserFinancialsRequest(userId));

        assertThat(response.totalEarnings()).isEqualByComparingTo("19.99");
        assertThat(response.paymentEvents()).singleElement().satisfies(item -> {
            assertThat(item.productId()).isEqualTo(100L);
            assertThat(item.productName()).isEqualTo("Premium Plan");
            assertThat(item.productPrice()).isEqualByComparingTo("19.99");
            assertThat(item.status()).isEqualTo("APPROVED");
        });
    }

    @Test
    void execute_shouldThrowException_whenProductIsMissing() {
        Long userId = 1L;
        PaymentEvent payment = PaymentEvent.builder()
                .id(200L)
                .userId(userId)
                .productId(999L)
                .status(PaymentStatus.APPROVED)
                .productType(ProductType.PLAN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(AppUser.builder().id(userId).build()));
        when(paymentEventRepository.findByUserId(userId)).thenReturn(List.of(payment));
        when(productRepository.findByIds(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new GetUserFinancialsRequest(userId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing product for paymentEventId=200, productId=999");
    }

    @Test
    void execute_shouldThrowException_whenProductIdIsNull() {
        Long userId = 1L;
        PaymentEvent payment = PaymentEvent.builder()
                .id(200L)
                .userId(userId)
                .productId(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(AppUser.builder().id(userId).build()));
        when(paymentEventRepository.findByUserId(userId)).thenReturn(List.of(payment));
        when(productRepository.findByIds(List.of())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new GetUserFinancialsRequest(userId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Missing product for paymentEventId=200, productId=null");
    }

    @Test
    void execute_shouldHandleDuplicateProductIds() {
        Long userId = 1L;
        Product product = Product.builder()
                .id(100L)
                .name("Premium Plan")
                .price(new BigDecimal("19.99"))
                .type(ProductType.PLAN)
                .build();
        PaymentEvent payment1 = PaymentEvent.builder()
                .id(200L)
                .userId(userId)
                .productId(100L)
                .status(PaymentStatus.APPROVED)
                .productType(ProductType.PLAN)
                .createdAt(Instant.now())
                .build();
        PaymentEvent payment2 = PaymentEvent.builder()
                .id(201L)
                .userId(userId)
                .productId(100L)
                .status(PaymentStatus.APPROVED)
                .productType(ProductType.PLAN)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(AppUser.builder().id(userId).build()));
        when(paymentEventRepository.findByUserId(userId)).thenReturn(List.of(payment1, payment2));
        when(paymentEventRepository.findByUserIdAndStatus(userId, PaymentStatus.APPROVED)).thenReturn(List.of(payment1, payment2));
        when(productRepository.findByIds(List.of(100L))).thenReturn(List.of(product, product));

        GetUserFinancialsResponse response = useCase.execute(new GetUserFinancialsRequest(userId));

        assertThat(response.totalEarnings()).isEqualByComparingTo("39.98");
        assertThat(response.paymentEvents()).hasSize(2);
    }
}
