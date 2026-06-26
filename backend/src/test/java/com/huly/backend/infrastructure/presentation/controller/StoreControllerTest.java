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
import com.huly.backend.domain.useCase.store.*;
import com.huly.backend.infrastructure.presentation.exception.GlobalExceptionHandler;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
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
import java.math.BigDecimal;

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
                                unequipStoreItemUseCase,
                                new StorePresentationMapper());

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
                StoreItemView item = new StoreItemView(
                                10L, "Casa rosa", "Casa de color rosa", ItemCategory.HOUSE, "casa-rosa",
                                50, new BigDecimal("1000.00"), false);
                when(listStoreItemsUseCase.execute()).thenReturn(new ListStoreItemsResponse(List.of(item)));

                mockMvc.perform(get("/api/store/items"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].id").value(10))
                                .andExpect(jsonPath("$[0].assetKey").value("casa-rosa"))
                                .andExpect(jsonPath("$[0].category").value("HOUSE"))
                                .andExpect(jsonPath("$[0].priceCoins").value(50))
                                .andExpect(jsonPath("$[0].price").value(1000.00));
        }

        @Test
        void getInventory_shouldReturnMappedInventory() throws Exception {
                InventoryItemView item = new InventoryItemView(
                                10L, "Casa rosa", ItemCategory.HOUSE, "casa-rosa", true);
                when(getUserInventoryUseCase.execute(new GetUserInventoryRequest(USER_ID)))
                                .thenReturn(new GetUserInventoryResponse(List.of(item)));

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

                verify(buyStoreItemUseCase).execute(new BuyStoreItemRequest(USER_ID, 10L));
        }

        @Test
        void equip_shouldDelegateToUseCaseAndReturnOk() throws Exception {
                mockMvc.perform(post("/api/store/items/10/equip"))
                                .andExpect(status().isOk());

                verify(equipStoreItemUseCase).execute(new EquipStoreItemRequest(USER_ID, 10L));
        }

        @Test
        void unequip_shouldDelegateToUseCaseAndReturnOk() throws Exception {
                mockMvc.perform(post("/api/store/items/10/unequip"))
                                .andExpect(status().isOk());

                verify(unequipStoreItemUseCase).execute(new UnequipStoreItemRequest(USER_ID, 10L));
        }
}
