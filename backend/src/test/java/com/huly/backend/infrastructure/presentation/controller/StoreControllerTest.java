package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.useCase.store.*;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoreControllerTest {

    private MockMvc mockMvc;
    private ListStoreItemsUseCase listStoreItemsUseCase;
    private GetUserInventoryUseCase getUserInventoryUseCase;
    private BuyStoreItemUseCase buyStoreItemUseCase;
    private EquipStoreItemUseCase equipStoreItemUseCase;
    private UnequipStoreItemUseCase unequipStoreItemUseCase;

    private static final Long USER_ID = 7L;

    @BeforeEach
    void setUp() {
        listStoreItemsUseCase = mock(ListStoreItemsUseCase.class);
        getUserInventoryUseCase = mock(GetUserInventoryUseCase.class);
        buyStoreItemUseCase = mock(BuyStoreItemUseCase.class);
        equipStoreItemUseCase = mock(EquipStoreItemUseCase.class);
        unequipStoreItemUseCase = mock(UnequipStoreItemUseCase.class);

        UserDetails userDetails = new User(String.valueOf(USER_ID), "", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(userDetails, null));

        StoreController storeController = new StoreController(
                listStoreItemsUseCase,
                getUserInventoryUseCase,
                buyStoreItemUseCase,
                equipStoreItemUseCase,
                unequipStoreItemUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getItems_shouldReturnMappedCatalog() throws Exception {
        StoreItem item = StoreItem.builder()
                .id(10L).name("Casa rosa").description("Casa de color rosa")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                .build();
        when(listStoreItemsUseCase.execute()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/store/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].assetKey").value("casa-rosa"))
                .andExpect(jsonPath("$[0].category").value("HOUSE"))
                .andExpect(jsonPath("$[0].priceCoins").value(50));
    }

    @Test
    void getInventory_shouldReturnMappedInventory() throws Exception {
        StoreItem item = StoreItem.builder()
                .id(10L).name("Casa rosa").description("desc")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                .build();
        UserStoreItem owned = UserStoreItem.builder().id(1L).userId(USER_ID).storeItem(item).equipped(true).build();
        when(getUserInventoryUseCase.execute(USER_ID)).thenReturn(List.of(owned));

        mockMvc.perform(get("/api/store/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].storeItemId").value(10))
                .andExpect(jsonPath("$[0].assetKey").value("casa-rosa"))
                .andExpect(jsonPath("$[0].equipped").value(true));
    }

    @Test
    void buy_shouldDelegateToUseCaseAndReturnOk() throws Exception {
        mockMvc.perform(post("/api/store/items/10/buy"))
                .andExpect(status().isOk());

        verify(buyStoreItemUseCase).execute(USER_ID, 10L);
    }

    @Test
    void equip_shouldDelegateToUseCaseAndReturnOk() throws Exception {
        mockMvc.perform(post("/api/store/items/10/equip"))
                .andExpect(status().isOk());

        verify(equipStoreItemUseCase).execute(USER_ID, 10L);
    }

    @Test
    void unequip_shouldDelegateToUseCaseAndReturnOk() throws Exception {
        mockMvc.perform(post("/api/store/items/10/unequip"))
                .andExpect(status().isOk());

        verify(unequipStoreItemUseCase).execute(USER_ID, 10L);
    }
}
