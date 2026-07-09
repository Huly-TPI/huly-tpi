package com.huly.backend.domain.useCase.store;

import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.dto.store.UpdateStoreItemRequest;
import com.huly.backend.domain.mapper.store.StoreItemMapper;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.repository.StoreItemRepository;
import com.huly.backend.domain.service.store.StoreItemImageService;
import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStoreItemUseCaseTest {

    private static final Long ITEM_ID = 5L;
    private static final String OLD_IMAGE_URL = "https://x/store/light-theme/old.webp";
    private static final String NEW_IMAGE_URL = "http://x/light-theme/new.webp";

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private StoreItemImageService imageService;

    private UpdateStoreItemUseCase useCase;

    private UpdateStoreItemRequest request;

    @BeforeEach
    void setUp() {
        useCase = new UpdateStoreItemUseCase(storeItemRepository, new StoreItemMapper(), imageService);
    }

    @Test
    @DisplayName("Lanza NotFound cuando el item no existe")
    void executeShouldThrowWhenItemNotFound() {
        givenItemNotFound();

        thenUpdateThrowsNotFound();
    }

    @Test
    @DisplayName("Sube las nuevas imágenes cuando se proveen")
    void executeShouldUploadNewImagesWhenProvided() {
        givenExistingItemAndNewImagesProvided();

        StoreItemView result = update();

        thenResponseUsesNewImage(result);
    }

    @Test
    @DisplayName("Conserva la imagen anterior cuando no se proveen nuevas")
    void executeShouldKeepOldImageWhenNotProvided() {
        givenExistingItemWithoutNewImages();

        StoreItemView result = update();

        thenResponseKeepsOldImage(result);
    }

    // --- arrange ---

    private void givenItemNotFound() {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());
        request = request(null, null);
    }

    private void givenExistingItemAndNewImagesProvided() {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(existing()));
        when(imageService.uploadThemePair(any(), any(), any())).thenReturn(NEW_IMAGE_URL);
        when(storeItemRepository.save(any(StoreItem.class))).thenAnswer(inv -> inv.getArgument(0));
        request = request(new byte[] { 1 }, new byte[] { 2 });
    }

    private void givenExistingItemWithoutNewImages() {
        when(storeItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(existing()));
        when(storeItemRepository.save(any(StoreItem.class))).thenAnswer(inv -> inv.getArgument(0));
        request = request(null, null);
    }

    private StoreItem existing() {
        return StoreItem.builder()
                .id(ITEM_ID)
                .assetKey("casa-rosa")
                .name("viejo")
                .description("d")
                .category(ItemCategory.HOUSE)
                .priceCoins(50)
                .imageUrl(OLD_IMAGE_URL)
                .build();
    }

    private UpdateStoreItemRequest request(byte[] light, byte[] dark) {
        return new UpdateStoreItemRequest(
                ITEM_ID,
                "nuevo",
                "desc2",
                ItemCategory.TREE,
                120,
                new BigDecimal("900.00"),
                true,
                light,
                dark,
                "image/webp");
    }

    // --- act ---

    private StoreItemView update() {
        return useCase.execute(request);
    }

    // --- assert ---

    private void thenUpdateThrowsNotFound() {
        assertThatThrownBy(this::update).isInstanceOf(NotFoundException.class);
    }

    private void thenResponseUsesNewImage(StoreItemView result) {
        verify(imageService).uploadThemePair(any(), any(), any());
        assertThat(result.imageUrl()).isEqualTo(NEW_IMAGE_URL);
        assertThat(result.assetKey()).isEqualTo("casa-rosa");
        assertThat(result.name()).isEqualTo("nuevo");
        assertThat(result.category()).isEqualTo(ItemCategory.TREE);
        assertThat(result.priceCoins()).isEqualTo(120);
    }

    private void thenResponseKeepsOldImage(StoreItemView result) {
        verifyNoInteractions(imageService);
        assertThat(result.imageUrl()).isEqualTo(OLD_IMAGE_URL);
    }
}
