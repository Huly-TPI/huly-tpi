package com.huly.backend.presentation.controller;

import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListPlansUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import com.huly.backend.infrastructure.presentation.controller.PaymentController;
import com.huly.backend.infrastructure.presentation.dto.payment.CreatePreferenceResponse;
import com.huly.backend.infrastructure.presentation.dto.payment.PlanResponse;
import com.huly.backend.infrastructure.presentation.dto.payment.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private ListProductsUseCase listProductsUseCase;
    @Mock private ListPlansUseCase listPlansUseCase;
    @Mock private CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase;

    @InjectMocks private PaymentController paymentController;

    private UserDetails principalWithId(Long id) {
        return new User(String.valueOf(id), "ignored", Collections.emptyList());
    }

    @Test
    void getProducts_shouldMapDomainProductsToResponse() {
        Product product = Product.builder()
                .id(1L).name("Pack Inicial").description("100 monedas")
                .price(new BigDecimal("499")).coinsAmount(100)
                .type(ProductType.COIN_PACK).build();
        when(listProductsUseCase.execute()).thenReturn(List.of(product));

        ResponseEntity<List<ProductResponse>> response = paymentController.getProducts();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        ProductResponse body = response.getBody().get(0);
        assertThat(body.id()).isEqualTo("1");
        assertThat(body.name()).isEqualTo("Pack Inicial");
        assertThat(body.description()).isEqualTo("100 monedas");
        assertThat(body.price()).isEqualByComparingTo("499");
        assertThat(body.coinsAmount()).isEqualTo(100);
    }

    @Test
    void getProducts_shouldReturnEmptyList_whenNoProducts() {
        when(listProductsUseCase.execute()).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProductResponse>> response = paymentController.getProducts();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getPlans_shouldMapDomainPlansToResponse() {
        Product plan = Product.builder()
                .id(10L).name("Plan Premium").description("Acceso premium")
                .price(new BigDecimal("9999")).coinsAmount(0)
                .type(ProductType.PLAN).planCode("PREMIUM").build();
        when(listPlansUseCase.execute()).thenReturn(List.of(plan));

        ResponseEntity<List<PlanResponse>> response = paymentController.getPlans();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        PlanResponse body = response.getBody().get(0);
        assertThat(body.id()).isEqualTo("10");
        assertThat(body.name()).isEqualTo("Plan Premium");
        assertThat(body.description()).isEqualTo("Acceso premium");
        assertThat(body.price()).isEqualByComparingTo("9999");
        assertThat(body.planCode()).isEqualTo("PREMIUM");
    }

    @Test
    void getPlans_shouldReturnEmptyList_whenNoPlans() {
        when(listPlansUseCase.execute()).thenReturn(Collections.emptyList());

        ResponseEntity<List<PlanResponse>> response = paymentController.getPlans();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void createPreference_shouldReturnPreferenceForAuthenticatedUser() {
        when(createPaymentPreferenceUseCase.execute(5L, 10L))
                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));

        ResponseEntity<CreatePreferenceResponse> response =
                paymentController.createPreference(5L, principalWithId(10L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().preferenceId()).isEqualTo("pref-123");
        assertThat(response.getBody().initPoint()).isEqualTo("https://mp.com/checkout");
        verify(createPaymentPreferenceUseCase).execute(5L, 10L);
    }
}
