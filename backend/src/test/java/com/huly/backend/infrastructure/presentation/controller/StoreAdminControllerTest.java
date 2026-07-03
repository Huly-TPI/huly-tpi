package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.useCase.store.CreateStoreItemUseCase;
import com.huly.backend.domain.useCase.store.DeleteStoreItemUseCase;
import com.huly.backend.domain.useCase.store.UpdateStoreItemUseCase;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoreAdminControllerTest {

    private MockMvc mockMvc;
    private CreateStoreItemUseCase createStoreItemUseCase;
    private UpdateStoreItemUseCase updateStoreItemUseCase;
    private DeleteStoreItemUseCase deleteStoreItemUseCase;
    private StorePresentationMapper mapper;

    @BeforeEach
    void setUp() {
        createStoreItemUseCase = mock(CreateStoreItemUseCase.class);
        updateStoreItemUseCase = mock(UpdateStoreItemUseCase.class);
        deleteStoreItemUseCase = mock(DeleteStoreItemUseCase.class);
        mapper = new StorePresentationMapper();

        StoreAdminController controller = new StoreAdminController(
                createStoreItemUseCase, updateStoreItemUseCase, deleteStoreItemUseCase, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private StoreItemView view(Long id) {
        return new StoreItemView(id, "Casa nueva", "desc", ItemCategory.HOUSE, "k",
                80, new BigDecimal("500.00"), false, "http://x/light-theme/u.webp");
    }

    private MockMultipartFile lightImage() {
        return new MockMultipartFile("imageLight", "l.webp", "image/webp", new byte[] { 1 });
    }

    private MockMultipartFile darkImage() {
        return new MockMultipartFile("imageDark", "d.webp", "image/webp", new byte[] { 2 });
    }

    @Test
    void create_shouldReturn201_withMappedResponse() throws Exception {
        when(createStoreItemUseCase.execute(any())).thenReturn(view(7L));

        mockMvc.perform(multipart("/api/admin/store/items")
                .file(lightImage())
                .file(darkImage())
                .param("name", "Casa nueva")
                .param("description", "desc")
                .param("category", "HOUSE")
                .param("priceCoins", "80")
                .param("price", "500.00")
                .param("premiumOnly", "false"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.imageUrlLight").value("http://x/light-theme/u.webp"))
                .andExpect(jsonPath("$.imageUrlDark").value("http://x/dark-theme/u.webp"));

        verify(createStoreItemUseCase).execute(any());
    }

    @Test
    void update_shouldReturn200_withMappedResponse() throws Exception {
        when(updateStoreItemUseCase.execute(any())).thenReturn(view(5L));

        mockMvc.perform(multipart("/api/admin/store/items/5")
                .file(lightImage())
                .file(darkImage())
                .param("name", "Casa editada")
                .param("description", "desc")
                .param("category", "HOUSE")
                .param("priceCoins", "90")
                .param("premiumOnly", "true")
                .with(req -> {
                    req.setMethod("PUT");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(updateStoreItemUseCase).execute(any());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/admin/store/items/9"))
                .andExpect(status().isNoContent());

        verify(deleteStoreItemUseCase).execute(any());
    }

    @Test
    void update_shouldWork_withoutNewImages() throws Exception {
        when(updateStoreItemUseCase.execute(any())).thenReturn(view(5L));

        mockMvc.perform(multipart("/api/admin/store/items/5")
                .param("name", "Casa editada")
                .param("description", "desc")
                .param("category", "HOUSE")
                .param("priceCoins", "90")
                .param("premiumOnly", "false")
                .with(req -> {
                    req.setMethod("PUT");
                    return req;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(updateStoreItemUseCase).execute(any());
    }

}
