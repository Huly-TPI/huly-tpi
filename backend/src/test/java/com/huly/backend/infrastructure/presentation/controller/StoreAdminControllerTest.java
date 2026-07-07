package com.huly.backend.infrastructure.presentation.controller;

import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.useCase.store.CreateStoreItemUseCase;
import com.huly.backend.domain.useCase.store.DeleteStoreItemUseCase;
import com.huly.backend.domain.useCase.store.UpdateStoreItemUseCase;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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

    @Test
    @DisplayName("Crea un item y devuelve 201 con la respuesta mapeada")
    void createShouldReturn201WithMappedResponse() throws Exception {
        givenCreateReturns(view(7L));

        ResultActions result = performCreate();

        thenCreatedWithImageUrls(result);
        thenCreateWasInvoked();
    }

    @Test
    @DisplayName("Actualiza un item y devuelve 200 con la respuesta mapeada")
    void updateShouldReturn200WithMappedResponse() throws Exception {
        givenUpdateReturns(view(5L));

        ResultActions result = performUpdate();

        thenOkWithId(result, 5);
        thenUpdateWasInvoked();
    }

    @Test
    @DisplayName("Elimina un item y devuelve 204")
    void deleteShouldReturn204() throws Exception {
        ResultActions result = performDelete();

        thenNoContent(result);
        thenDeleteWasInvoked();
    }

    @Test
    @DisplayName("Actualiza un item sin imágenes nuevas y devuelve 200")
    void updateShouldWorkWithoutNewImages() throws Exception {
        givenUpdateReturns(view(5L));

        ResultActions result = performUpdateWithoutImages();

        thenOkWithId(result, 5);
        thenUpdateWasInvoked();
    }

    // --- arrange ---
    private void givenCreateReturns(StoreItemView view) {
        when(createStoreItemUseCase.execute(any())).thenReturn(view);
    }

    private void givenUpdateReturns(StoreItemView view) {
        when(updateStoreItemUseCase.execute(any())).thenReturn(view);
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

    // --- act ---
    private ResultActions performCreate() throws Exception {
        return mockMvc.perform(multipart("/api/admin/store/items")
                .file(lightImage())
                .file(darkImage())
                .param("name", "Casa nueva")
                .param("description", "desc")
                .param("category", "HOUSE")
                .param("priceCoins", "80")
                .param("price", "500.00")
                .param("premiumOnly", "false"));
    }

    private ResultActions performUpdate() throws Exception {
        return mockMvc.perform(multipart("/api/admin/store/items/5")
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
                }));
    }

    private ResultActions performDelete() throws Exception {
        return mockMvc.perform(delete("/api/admin/store/items/9"));
    }

    private ResultActions performUpdateWithoutImages() throws Exception {
        return mockMvc.perform(multipart("/api/admin/store/items/5")
                .param("name", "Casa editada")
                .param("description", "desc")
                .param("category", "HOUSE")
                .param("priceCoins", "90")
                .param("premiumOnly", "false")
                .with(req -> {
                    req.setMethod("PUT");
                    return req;
                }));
    }

    // --- assert ---
    private void thenCreatedWithImageUrls(ResultActions result) throws Exception {
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.imageUrlLight").value("http://x/light-theme/u.webp"))
                .andExpect(jsonPath("$.imageUrlDark").value("http://x/dark-theme/u.webp"));
    }

    private void thenOkWithId(ResultActions result, int id) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    private void thenNoContent(ResultActions result) throws Exception {
        result.andExpect(status().isNoContent());
    }

    private void thenCreateWasInvoked() {
        verify(createStoreItemUseCase).execute(any());
    }

    private void thenUpdateWasInvoked() {
        verify(updateStoreItemUseCase).execute(any());
    }

    private void thenDeleteWasInvoked() {
        verify(deleteStoreItemUseCase).execute(any());
    }
}
