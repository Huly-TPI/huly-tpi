package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.enums.ProductType;
import com.huly.backend.domain.model.payment.PaymentPreferenceResult;
import com.huly.backend.domain.model.payment.Product;
import com.huly.backend.domain.useCase.payment.CreatePaymentPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.CreateStoreItemPreferenceUseCase;
import com.huly.backend.domain.useCase.payment.ListPlansUseCase;
import com.huly.backend.domain.useCase.payment.ListProductsUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    private static final Long USER_ID = 10L;

    private MockMvc mockMvc;
    private ListProductsUseCase listProductsUseCase;
    private ListPlansUseCase listPlansUseCase;
    private CreatePaymentPreferenceUseCase createPaymentPreferenceUseCase;
    private CreateStoreItemPreferenceUseCase createStoreItemPreferenceUseCase;

    @BeforeEach
    void setUp() {
        listProductsUseCase = mock(ListProductsUseCase.class);
        listPlansUseCase = mock(ListPlansUseCase.class);
        createPaymentPreferenceUseCase = mock(CreatePaymentPreferenceUseCase.class);
        createStoreItemPreferenceUseCase = mock(CreateStoreItemPreferenceUseCase.class);
        PaymentController controller = new PaymentController(
                listProductsUseCase,
                listPlansUseCase,
                createPaymentPreferenceUseCase,
                createStoreItemPreferenceUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authenticateAs(String.valueOf(USER_ID));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Mapea los productos de dominio a la respuesta")
    void getProductsShouldMapDomainProductsToResponse() throws Exception {
        // --- arrange ---
        givenProducts(List.of(coinPackProduct()));

        // --- act ---
        ResultActions result = performGetProducts();

        // --- assert ---
        thenOkWithProduct(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacia cuando no hay productos")
    void getProductsShouldReturnEmptyListWhenNoProducts() throws Exception {
        // --- arrange ---
        givenProducts(Collections.emptyList());

        // --- act ---
        ResultActions result = performGetProducts();

        // --- assert ---
        thenOkWithEmptyArray(result);
    }

    @Test
    @DisplayName("Mapea los planes de dominio a la respuesta")
    void getPlansShouldMapDomainPlansToResponse() throws Exception {
        // --- arrange ---
        givenPlans(List.of(premiumPlan()));

        // --- act ---
        ResultActions result = performGetPlans();

        // --- assert ---
        thenOkWithPlan(result);
    }

    @Test
    @DisplayName("Devuelve una lista vacia cuando no hay planes")
    void getPlansShouldReturnEmptyListWhenNoPlans() throws Exception {
        // --- arrange ---
        givenPlans(Collections.emptyList());

        // --- act ---
        ResultActions result = performGetPlans();

        // --- assert ---
        thenOkWithEmptyArray(result);
    }

    @Test
    @DisplayName("Devuelve la preferencia de pago para el usuario autenticado")
    void createPreferenceShouldReturnPreferenceForAuthenticatedUser() throws Exception {
        // --- arrange ---
        givenPaymentPreference(5L, new PaymentPreferenceResult("pref-123", "https://mp.com/checkout"));

        // --- act ---
        ResultActions result = performCreatePreference(5L);

        // --- assert ---
        thenOkWithPreference(result, "pref-123", "https://mp.com/checkout");
        thenPaymentPreferenceCreatedFor(5L, USER_ID);
    }

    @Test
    @DisplayName("Devuelve la preferencia de item de tienda para el usuario autenticado")
    void createStoreItemPreferenceShouldReturnPreferenceForAuthenticatedUser() throws Exception {
        // --- arrange ---
        givenStoreItemPreference(3L,
                new PaymentPreferenceResult("pref-store-1", "https://mp.com/checkout-store"));

        // --- act ---
        ResultActions result = performCreateStoreItemPreference(3L);

        // --- assert ---
        thenOkWithPreference(result, "pref-store-1", "https://mp.com/checkout-store");
        thenStoreItemPreferenceCreatedFor(3L, USER_ID);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenProducts(List<Product> products) {
        when(listProductsUseCase.execute()).thenReturn(products);
    }

    private void givenPlans(List<Product> plans) {
        when(listPlansUseCase.execute()).thenReturn(plans);
    }

    private void givenPaymentPreference(Long productId, PaymentPreferenceResult preference) {
        when(createPaymentPreferenceUseCase.execute(productId, USER_ID)).thenReturn(preference);
    }

    private void givenStoreItemPreference(Long storeItemId, PaymentPreferenceResult preference) {
        when(createStoreItemPreferenceUseCase.execute(storeItemId, USER_ID)).thenReturn(preference);
    }

    private Product coinPackProduct() {
        return Product.builder()
                .id(1L).name("Pack Inicial").description("100 monedas")
                .price(new BigDecimal("499")).coinsAmount(100)
                .type(ProductType.COIN_PACK).build();
    }

    private Product premiumPlan() {
        return Product.builder()
                .id(10L).name("Plan Premium").description("Acceso premium")
                .price(new BigDecimal("9999")).coinsAmount(500)
                .type(ProductType.PLAN).planCode("PREMIUM").build();
    }

    // --- act ---
    private ResultActions performGetProducts() throws Exception {
        return mockMvc.perform(get("/api/payment/products"));
    }

    private ResultActions performGetPlans() throws Exception {
        return mockMvc.perform(get("/api/payment/plans"));
    }

    private ResultActions performCreatePreference(long productId) throws Exception {
        return mockMvc.perform(post("/api/payment/preference/" + productId));
    }

    private ResultActions performCreateStoreItemPreference(long storeItemId) throws Exception {
        return mockMvc.perform(post("/api/payment/store-preference/" + storeItemId));
    }

    // --- assert ---
    private void thenOkWithProduct(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Pack Inicial"))
                .andExpect(jsonPath("$[0].description").value("100 monedas"))
                .andExpect(jsonPath("$[0].price").value(499))
                .andExpect(jsonPath("$[0].coinsAmount").value(100));
    }

    private void thenOkWithPlan(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("10"))
                .andExpect(jsonPath("$[0].name").value("Plan Premium"))
                .andExpect(jsonPath("$[0].description").value("Acceso premium"))
                .andExpect(jsonPath("$[0].price").value(9999))
                .andExpect(jsonPath("$[0].coinsAmount").value(500))
                .andExpect(jsonPath("$[0].planCode").value("PREMIUM"));
    }

    private void thenOkWithEmptyArray(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    private void thenOkWithPreference(ResultActions result, String preferenceId, String initPoint)
            throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.preferenceId").value(preferenceId))
                .andExpect(jsonPath("$.initPoint").value(initPoint));
    }

    private void thenPaymentPreferenceCreatedFor(Long productId, Long userId) {
        verify(createPaymentPreferenceUseCase).execute(productId, userId);
    }

    private void thenStoreItemPreferenceCreatedFor(Long storeItemId, Long userId) {
        verify(createStoreItemPreferenceUseCase).execute(storeItemId, userId);
    }
}
