package com.huly.backend.presentation.controller;

import com.huly.backend.domain.dto.payment.PaymentPreferenceResult;
import com.huly.backend.domain.dto.payment.Product;
import com.huly.backend.domain.exception.ResourceNotFoundException;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import com.huly.backend.infrastructure.presentation.controller.PaymentController;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerTest {

    private MockMvc mockMvc;
    private ListProductsUseCase listProductsUseCase;
    private CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase;

    @BeforeEach
    void setUp() {
        listProductsUseCase = mock(ListProductsUseCase.class);
        createPaymentPreferenceUseCase = mock(CreatePaymentPreferenceUseCase.class);

        PaymentController controller = new PaymentController(listProductsUseCase, createPaymentPreferenceUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getProducts_shouldReturn200_withProductList() throws Exception {
        List<Product> products = List.of(
                Product.builder().id(1L).name("Pack Inicial").description("100 monedas")
                        .price(new BigDecimal("499")).coinsAmount(100).build(),
                Product.builder().id(2L).name("Pack Estándar").description("500 monedas")
                        .price(new BigDecimal("1999")).coinsAmount(500).build()
        );
        when(listProductsUseCase.execute()).thenReturn(products);

        mockMvc.perform(get("/api/payment/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Pack Inicial"))
                .andExpect(jsonPath("$[0].coinsAmount").value(100))
                .andExpect(jsonPath("$[1].name").value("Pack Estándar"));
    }

    @Test
    void getProducts_shouldReturn200_withEmptyList_whenNoProductsExist() throws Exception {
        when(listProductsUseCase.execute()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/payment/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createPreference_shouldReturn200_withPreferenceIdAndInitPoint() throws Exception {
        when(createPaymentPreferenceUseCase.execute(1L, 10L))
                .thenReturn(new PaymentPreferenceResult("pref-abc", "https://mp.com/checkout"));

        mockMvc.perform(post("/api/payment/preference/1")
                        .with(user("10").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferenceId").value("pref-abc"))
                .andExpect(jsonPath("$.initPoint").value("https://mp.com/checkout"));

        verify(createPaymentPreferenceUseCase).execute(1L, 10L);
    }

    @Test
    void createPreference_shouldReturn404_whenProductNotFound() throws Exception {
        when(createPaymentPreferenceUseCase.execute(99L, 10L))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(post("/api/payment/preference/99")
                        .with(user("10").roles("USER")))
                .andExpect(status().isNotFound());
    }
}
