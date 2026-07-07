package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.store.BuyStoreItemRequest;
import com.huly.backend.domain.dto.store.EquipStoreItemRequest;
import com.huly.backend.domain.dto.store.GetUserInventoryRequest;
import com.huly.backend.domain.dto.store.GetUserInventoryResponse;
import com.huly.backend.domain.dto.store.InventoryItemView;
import com.huly.backend.domain.dto.store.ListStoreItemsResponse;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.dto.store.UnequipStoreItemRequest;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.useCase.store.BuyStoreItemUseCase;
import com.huly.backend.domain.useCase.store.EquipStoreItemUseCase;
import com.huly.backend.domain.useCase.store.GetUserInventoryUseCase;
import com.huly.backend.domain.useCase.store.ListStoreItemsUseCase;
import com.huly.backend.domain.useCase.store.UnequipStoreItemUseCase;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
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

class StoreControllerTest {

    private static final Long USER_ID = 7L;

    private MockMvc mockMvc;
    private ListStoreItemsUseCase listStoreItemsUseCase;
    private GetUserInventoryUseCase getUserInventoryUseCase;
    private BuyStoreItemUseCase buyStoreItemUseCase;
    private EquipStoreItemUseCase equipStoreItemUseCase;
    private UnequipStoreItemUseCase unequipStoreItemUseCase;

    @BeforeEach
    void setUp() {
        listStoreItemsUseCase = mock(ListStoreItemsUseCase.class);
        getUserInventoryUseCase = mock(GetUserInventoryUseCase.class);
        buyStoreItemUseCase = mock(BuyStoreItemUseCase.class);
        equipStoreItemUseCase = mock(EquipStoreItemUseCase.class);
        unequipStoreItemUseCase = mock(UnequipStoreItemUseCase.class);
        StoreController controller = new StoreController(
                listStoreItemsUseCase,
                getUserInventoryUseCase,
                buyStoreItemUseCase,
                equipStoreItemUseCase,
                unequipStoreItemUseCase,
                new StorePresentationMapper());
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
    @DisplayName("Devuelve el catalogo de items mapeado")
    void getItemsShouldReturnMappedCatalog() throws Exception {
        // --- arrange ---
        givenCatalog(storeItemView());

        // --- act ---
        ResultActions result = performGetItems();

        // --- assert ---
        thenOkWithCatalog(result);
    }

    @Test
    @DisplayName("Devuelve el inventario del usuario mapeado")
    void getInventoryShouldReturnMappedInventory() throws Exception {
        // --- arrange ---
        givenInventory(inventoryItemView());

        // --- act ---
        ResultActions result = performGetInventory();

        // --- assert ---
        thenOkWithInventory(result);
    }

    @Test
    @DisplayName("Delega la compra al use case y devuelve 200")
    void buyShouldDelegateToUseCaseAndReturnOk() throws Exception {
        // --- act ---
        ResultActions result = performBuy(10L);

        // --- assert ---
        thenOk(result);
        thenItemBought(10L);
    }

    @Test
    @DisplayName("Delega el equipar al use case y devuelve 200")
    void equipShouldDelegateToUseCaseAndReturnOk() throws Exception {
        // --- act ---
        ResultActions result = performEquip(10L);

        // --- assert ---
        thenOk(result);
        thenItemEquipped(10L);
    }

    @Test
    @DisplayName("Delega el desequipar al use case y devuelve 200")
    void unequipShouldDelegateToUseCaseAndReturnOk() throws Exception {
        // --- act ---
        ResultActions result = performUnequip(10L);

        // --- assert ---
        thenOk(result);
        thenItemUnequipped(10L);
    }

    // --- arrange ---
    private void authenticateAs(String username) {
        UserDetails userDetails = new User(username, "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userDetails, null));
    }

    private void givenCatalog(StoreItemView item) {
        when(listStoreItemsUseCase.execute()).thenReturn(new ListStoreItemsResponse(List.of(item)));
    }

    private void givenInventory(InventoryItemView item) {
        when(getUserInventoryUseCase.execute(new GetUserInventoryRequest(USER_ID)))
                .thenReturn(new GetUserInventoryResponse(List.of(item)));
    }

    private StoreItemView storeItemView() {
        return new StoreItemView(
                10L, "Casa rosa", "Casa de color rosa", ItemCategory.HOUSE, "casa-rosa",
                50, new BigDecimal("1000.00"), false, null);
    }

    private InventoryItemView inventoryItemView() {
        return new InventoryItemView(
                10L, "Casa rosa", ItemCategory.HOUSE, "casa-rosa", true, null);
    }

    // --- act ---
    private ResultActions performGetItems() throws Exception {
        return mockMvc.perform(get("/api/store/items"));
    }

    private ResultActions performGetInventory() throws Exception {
        return mockMvc.perform(get("/api/store/inventory"));
    }

    private ResultActions performBuy(long itemId) throws Exception {
        return mockMvc.perform(post("/api/store/items/" + itemId + "/buy"));
    }

    private ResultActions performEquip(long itemId) throws Exception {
        return mockMvc.perform(post("/api/store/items/" + itemId + "/equip"));
    }

    private ResultActions performUnequip(long itemId) throws Exception {
        return mockMvc.perform(post("/api/store/items/" + itemId + "/unequip"));
    }

    // --- assert ---
    private void thenOk(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

    private void thenOkWithCatalog(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].assetKey").value("casa-rosa"))
                .andExpect(jsonPath("$[0].category").value("HOUSE"))
                .andExpect(jsonPath("$[0].priceCoins").value(50))
                .andExpect(jsonPath("$[0].price").value(1000.00));
    }

    private void thenOkWithInventory(ResultActions result) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].storeItemId").value(10))
                .andExpect(jsonPath("$[0].assetKey").value("casa-rosa"))
                .andExpect(jsonPath("$[0].equipped").value(true));
    }

    private void thenItemBought(Long itemId) {
        verify(buyStoreItemUseCase).execute(new BuyStoreItemRequest(USER_ID, itemId));
    }

    private void thenItemEquipped(Long itemId) {
        verify(equipStoreItemUseCase).execute(new EquipStoreItemRequest(USER_ID, itemId));
    }

    private void thenItemUnequipped(Long itemId) {
        verify(unequipStoreItemUseCase).execute(new UnequipStoreItemRequest(USER_ID, itemId));
    }
}
