package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.CreateStoreItemPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListPlansUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

        private MockMvc mockMvc;
        private ListProductsUseCase listProductsUseCase;
        private ListPlansUseCase listPlansUseCase;
        private CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase;
        private CreateStoreItemPreferenceUseCase createStoreItemPreferenceUseCase;

        private static final Long USER_ID = 10L;

        @BeforeEach
        void setUp() {
                listProductsUseCase = mock(ListProductsUseCase.class);
                listPlansUseCase = mock(ListPlansUseCase.class);
                createPaymentPreferenceUseCase = mock(CreatePaymentPreferenceUseCase.class);
                createStoreItemPreferenceUseCase = mock(CreateStoreItemPreferenceUseCase.class);
                UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(
                                new TestingAuthenticationToken(userDetails, null));

                PaymentController paymentController = new PaymentController(
                                listProductsUseCase,
                                listPlansUseCase,
                                createPaymentPreferenceUseCase,
                                createStoreItemPreferenceUseCase);

                mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        @Test
        void getProducts_shouldMapDomainProductsToResponse() throws Exception {
                Product product = Product.builder()
                                .id(1L).name("Pack Inicial").description("100 monedas")
                                .price(new BigDecimal("499")).coinsAmount(100)
                                .type(ProductType.COIN_PACK).build();
                when(listProductsUseCase.execute()).thenReturn(List.of(product));

                mockMvc.perform(get("/api/payment/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value("1"))
                                .andExpect(jsonPath("$[0].name").value("Pack Inicial"))
                                .andExpect(jsonPath("$[0].description").value("100 monedas"))
                                .andExpect(jsonPath("$[0].price").value(499))
                                .andExpect(jsonPath("$[0].coinsAmount").value(100));
        }

        @Test
        void getProducts_shouldReturnEmptyList_whenNoProducts() throws Exception {
                when(listProductsUseCase.execute()).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/payment/products"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void getPlans_shouldMapDomainPlansToResponse() throws Exception {
                Product plan = Product.builder()
                                .id(10L).name("Plan Premium").description("Acceso premium")
                                .price(new BigDecimal("9999")).coinsAmount(500)
                                .type(ProductType.PLAN).planCode("PREMIUM").build();
                when(listPlansUseCase.execute()).thenReturn(List.of(plan));

                mockMvc.perform(get("/api/payment/plans"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value("10"))
                                .andExpect(jsonPath("$[0].name").value("Plan Premium"))
                                .andExpect(jsonPath("$[0].description").value("Acceso premium"))
                                .andExpect(jsonPath("$[0].price").value(9999))
                                .andExpect(jsonPath("$[0].coinsAmount").value(500))
                                .andExpect(jsonPath("$[0].planCode").value("PREMIUM"));
        }

        @Test
        void getPlans_shouldReturnEmptyList_whenNoPlans() throws Exception {
                when(listPlansUseCase.execute()).thenReturn(Collections.emptyList());

                mockMvc.perform(get("/api/payment/plans"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void createPreference_shouldReturnPreferenceForAuthenticatedUser() throws Exception {
                when(createPaymentPreferenceUseCase.execute(5L, USER_ID))
                                .thenReturn(new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));

                mockMvc.perform(post("/api/payment/preference/5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.preferenceId").value("pref-123"))
                                .andExpect(jsonPath("$.initPoint").value("https://mp.com/checkout"));

                verify(createPaymentPreferenceUseCase).execute(5L, USER_ID);
        }

        @Test
        void createStoreItemPreference_shouldReturnPreferenceForAuthenticatedUser() throws Exception {
                when(createStoreItemPreferenceUseCase.execute(3L, USER_ID))
                                .thenReturn(new PaymentPreferenceResult("pref-store-1",
                                                "https://mp.com/checkout-store"));

                mockMvc.perform(post("/api/payment/store-preference/3"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.preferenceId").value("pref-store-1"))
                                .andExpect(jsonPath("$.initPoint").value("https://mp.com/checkout-store"));

                verify(createStoreItemPreferenceUseCase).execute(3L, USER_ID);
        }
}
