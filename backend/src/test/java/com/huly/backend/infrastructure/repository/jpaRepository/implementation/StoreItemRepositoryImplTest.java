package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IStoreItemJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreItemRepositoryImplTest {

    @Mock
    private IStoreItemJpaRepository storeItemJpaRepository;

    @InjectMocks
    private StoreItemRepositoryImpl storeItemRepository;

    @Test
    @DisplayName("Mapea a dominio todos los ítems de la tienda")
    void findAllShouldReturnMappedList() {
        givenAllItems(entity());

        List<StoreItem> result = findAll();

        thenItemListMatches(result);
    }

    @Test
    @DisplayName("Devuelve el ítem por id cuando existe")
    void findByIdShouldReturnMappedItem() {
        givenItemById(10L, entity());

        Optional<StoreItem> result = findById(10L);

        thenItemPresent(result);
    }

    @Test
    @DisplayName("Devuelve vacío al buscar el ítem por id cuando no existe")
    void findByIdShouldReturnEmptyWhenNotFound() {
        givenItemById(99L, null);

        Optional<StoreItem> result = findById(99L);

        thenEmpty(result);
    }

    @Test
    @DisplayName("Persiste y devuelve el ítem mapeado al guardar")
    void saveShouldPersistAndReturnMappedItem() {
        givenSaved(entity());

        StoreItem result = save(domainItem());

        thenSavedItemMatches(result);
    }

    @Test
    @DisplayName("Delega el borrado por id al repositorio JPA")
    void deleteByIdShouldCallRepository() {
        deleteById(5L);

        thenDeletedById(5L);
    }

    // --- arrange ---
    private void givenAllItems(StoreItemEntity entity) {
        when(storeItemJpaRepository.findAllByOrderByIdAsc()).thenReturn(List.of(entity));
    }

    private void givenItemById(Long id, StoreItemEntity entity) {
        when(storeItemJpaRepository.findById(id)).thenReturn(Optional.ofNullable(entity));
    }

    private void givenSaved(StoreItemEntity entity) {
        when(storeItemJpaRepository.save(any(StoreItemEntity.class))).thenReturn(entity);
    }

    private StoreItemEntity entity() {
        return StoreItemEntity.builder()
                .id(10L).name("Casa rosa").description("Casa de color rosa")
                .category(ItemCategory.HOUSE).assetKey("casa-rosa").priceCoins(50)
                .price(new BigDecimal("1000.00"))
                .build();
    }

    private StoreItem domainItem() {
        return StoreItem.builder()
                .id(10L).name("Casa nueva").description("desc")
                .category(ItemCategory.HOUSE).priceCoins(80)
                .imageUrl("https://x/store/light-theme/u.webp")
                .build();
    }

    // --- act ---
    private List<StoreItem> findAll() {
        return storeItemRepository.findAll();
    }

    private Optional<StoreItem> findById(Long id) {
        return storeItemRepository.findById(id);
    }

    private StoreItem save(StoreItem item) {
        return storeItemRepository.save(item);
    }

    private void deleteById(Long id) {
        storeItemRepository.deleteById(id);
    }

    // --- assert ---
    private void thenItemListMatches(List<StoreItem> result) {
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("Casa rosa");
        assertThat(result.get(0).getAssetKey()).isEqualTo("casa-rosa");
        assertThat(result.get(0).getCategory()).isEqualTo(ItemCategory.HOUSE);
        assertThat(result.get(0).getPriceCoins()).isEqualTo(50);
        assertThat(result.get(0).getPriceCoins()).isEqualTo(50);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("1000.00");
    }

    private void thenItemPresent(Optional<StoreItem> result) {
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Casa rosa");
    }

    private void thenEmpty(Optional<StoreItem> result) {
        assertThat(result).isEmpty();
    }

    private void thenSavedItemMatches(StoreItem result) {
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Casa rosa");
    }

    private void thenDeletedById(Long id) {
        verify(storeItemJpaRepository).deleteById(id);
    }
}
