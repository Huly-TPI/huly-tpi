package com.huly.backend.infrastructure.repository.mapper;

import com.huly.backend.domain.dto.store.GetUserInventoryResponse;
import com.huly.backend.domain.dto.store.InventoryItemView;
import com.huly.backend.domain.dto.store.ListStoreItemsResponse;
import com.huly.backend.domain.dto.store.StoreItemView;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.infrastructure.presentation.dto.store.InventoryItemResponse;
import com.huly.backend.infrastructure.presentation.dto.store.StoreItemResponse;
import com.huly.backend.infrastructure.presentation.mapper.store.StorePresentationMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StorePresentationMapperTest {

    private final StorePresentationMapper mapper = new StorePresentationMapper();
    @Test
    void toStoreItemResponses_derivaDarkDesdeLight() {
        StoreItemView view = new StoreItemView(1L, "Casa", "d", ItemCategory.HOUSE, null,
                50, new BigDecimal("1.00"), false, "http://x/light-theme/u.webp");

        List<StoreItemResponse> res = mapper.toStoreItemResponses(new ListStoreItemsResponse(List.of(view)));

        assertThat(res.get(0).imageUrlLight()).isEqualTo("http://x/light-theme/u.webp");
        assertThat(res.get(0).imageUrlDark()).isEqualTo("http://x/dark-theme/u.webp");
    }

    @Test
    void toStoreItemResponses_conImageUrlNull_dejaNulls() {
        StoreItemView view = new StoreItemView(1L, "Casa", "d", ItemCategory.HOUSE, "casa-rosa",
                50, null, false, null);

        List<StoreItemResponse> res = mapper.toStoreItemResponses(new ListStoreItemsResponse(List.of(view)));

        assertThat(res.get(0).imageUrlLight()).isNull();
        assertThat(res.get(0).imageUrlDark()).isNull();
    }

    @Test
    void toInventoryResponses_derivaDarkDesdeLight() {
        InventoryItemView view = new InventoryItemView(1L, "Casa", ItemCategory.HOUSE, null, true,
                "http://x/light-theme/u.webp");

        List<InventoryItemResponse> res = mapper.toInventoryResponses(new GetUserInventoryResponse(List.of(view)));

        assertThat(res.get(0).imageUrlLight()).isEqualTo("http://x/light-theme/u.webp");
        assertThat(res.get(0).imageUrlDark()).isEqualTo("http://x/dark-theme/u.webp");
    }

    @Test
    void toInventoryResponses_conImageUrlNull_dejaNulls() {
        InventoryItemView view = new InventoryItemView(1L, "Casa", ItemCategory.HOUSE, "casa-rosa", true, null);

        List<InventoryItemResponse> res = mapper.toInventoryResponses(new GetUserInventoryResponse(List.of(view)));

        assertThat(res.get(0).imageUrlLight()).isNull();
        assertThat(res.get(0).imageUrlDark()).isNull();
    }
}