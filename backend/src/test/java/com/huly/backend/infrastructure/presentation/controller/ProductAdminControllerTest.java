package com.huly.backend.infrastructure.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.useCase.payment.CreateProductUseCase;
import com.huly.backend.domain.useCase.payment.ListAdminProductsUseCase;
import com.huly.backend.domain.useCase.payment.SetProductActiveUseCase;
import com.huly.backend.domain.useCase.payment.UpdateProductUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductAdminControllerTest {

    private MockMvc mockMvc;
    private ListAdminProductsUseCase listAdminProductsUseCase;
    private CreateProductUseCase createProductUseCase;
    private UpdateProductUseCase updateProductUseCase;
    private SetProductActiveUseCase setProductActiveUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Product product(Long id) {
        return Product.builder().id(id).name("Pack").description("d").price(new BigDecimal("499"))
                .coinsAmount(100).type(ProductType.COIN_PACK).active(true).build();
    }

    private Map<String, Object> body() {
        return Map.of("name", "Pack", "description", "d", "price", 499, "coinsAmount", 100, "type", "COIN_PACK");
    }

    @BeforeEach
    void setUp() {
        listAdminProductsUseCase = mock(ListAdminProductsUseCase.class);
        createProductUseCase = mock(CreateProductUseCase.class);
        updateProductUseCase = mock(UpdateProductUseCase.class);
        setProductActiveUseCase = mock(SetProductActiveUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductAdminController(
                listAdminProductsUseCase, createProductUseCase, updateProductUseCase, setProductActiveUseCase)).build();
    }

    @Test
    void list_shouldReturnProducts() throws Exception {
        when(listAdminProductsUseCase.execute(ProductType.COIN_PACK)).thenReturn(List.of(product(1L)));
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(createProductUseCase.execute(any())).thenReturn(product(7L));
        mockMvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
        verify(createProductUseCase).execute(any());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(updateProductUseCase.execute(any())).thenReturn(product(5L));
        mockMvc.perform(put("/api/admin/products/5").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
        verify(updateProductUseCase).execute(any());
    }

    @Test
    void setActive_shouldReturn200() throws Exception {
        when(setProductActiveUseCase.execute(eq(9L), eq(false))).thenReturn(product(9L));
        mockMvc.perform(patch("/api/admin/products/9/active").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("active", false))))
                .andExpect(status().isOk());
        verify(setProductActiveUseCase).execute(9L, false);
    }
}