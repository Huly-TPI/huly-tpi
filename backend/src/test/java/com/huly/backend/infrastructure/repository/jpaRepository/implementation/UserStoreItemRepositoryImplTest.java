package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.shop.StoreItem;
import com.huly.backend.domain.model.user.UserStoreItem;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import com.huly.backend.infrastructure.repository.entity.UserStoreItemEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IStoreItemJpaRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.IUserStoreItemJpaRepository;
import com.huly.backend.infrastructure.repository.mapper.UserStoreItemMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStoreItemRepositoryImplTest {

    private static final Long USER_ID = 7L;
    private static final Long STORE_ITEM_ID = 10L;

    @Mock
    private IUserStoreItemJpaRepository userStoreItemJpaRepository;
    @Mock
    private IStoreItemJpaRepository storeItemJpaRepository;
    @Mock
    private UserStoreItemMapper userStoreItemMapper;

    @InjectMocks
    private UserStoreItemRepositoryImpl userStoreItemRepository;

    @Test
    @DisplayName("Mapea la lista de items del usuario cuando hay datos")
    void findAllByUserIdShouldReturnMappedList() {
        UserStoreItemEntity entity = userItemEntity(1L);
        givenUserItems(entity);
        givenMappedDomain(entity, userItemDomain(1L));

        List<UserStoreItem> result = findAllByUserId();

        thenItemsHaveUserIds(result, USER_ID);
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando el usuario no tiene items")
    void findAllByUserIdShouldReturnEmptyWhenNone() {
        givenUserItems();

        List<UserStoreItem> result = findAllByUserId();

        thenItemsEmpty(result);
    }

    @Test
    @DisplayName("Delega la comprobación de propiedad de un item")
    void isOwnedShouldDelegateToJpaRepository() {
        givenOwnership(true);

        boolean result = isOwned();

        thenOwned(result);
    }

    @Test
    @DisplayName("Construye la entidad con la referencia al item y devuelve el dominio mapeado")
    void saveShouldBuildEntityWithReferenceAndReturnMapped() {
        Instant now = Instant.now();
        UserStoreItemEntity savedEntity = userItemEntity(99L);
        givenStoreItemReference(storeItemRef());
        givenSaved(savedEntity, userItemDomain(99L));

        UserStoreItem result = save(saveDomain(now));

        thenSavedEntityMatchesAndReturnsMapped(result, now, 99L);
    }

    @Test
    @DisplayName("Delega la actualización del estado equipado")
    void updateEquippedShouldDelegateToJpaRepository() {
        updateEquipped(true);

        thenUpdateEquippedDelegated(true);
    }

    @Test
    @DisplayName("Delega la búsqueda de asset keys por usuario y categoría cuando hay datos")
    void findAssetKeysByUserIdAndCategoryShouldDelegateWithData() {
        givenAssetKeys(ItemCategory.TREE, List.of("tree_1", "tree_2"));

        List<String> result = findAssetKeys(ItemCategory.TREE);

        thenAssetKeysAre(result, ItemCategory.TREE, "tree_1", "tree_2");
    }

    @Test
    @DisplayName("Devuelve lista vacía de asset keys cuando no hay coincidencias")
    void findAssetKeysByUserIdAndCategoryShouldReturnEmptyWhenNone() {
        givenAssetKeys(ItemCategory.HOUSE, List.of());

        List<String> result = findAssetKeys(ItemCategory.HOUSE);

        thenAssetKeysEmpty(result, ItemCategory.HOUSE);
    }

    // --- arrange ---
    private void givenUserItems(UserStoreItemEntity... entities) {
        when(userStoreItemJpaRepository.findAllByUserId(USER_ID)).thenReturn(List.of(entities));
    }

    private void givenMappedDomain(UserStoreItemEntity entity, UserStoreItem domain) {
        when(userStoreItemMapper.toDomain(entity)).thenReturn(domain);
    }

    private void givenOwnership(boolean owned) {
        when(userStoreItemJpaRepository.existsByUserIdAndStoreItemId(USER_ID, STORE_ITEM_ID)).thenReturn(owned);
    }

    private void givenStoreItemReference(StoreItemEntity ref) {
        when(storeItemJpaRepository.getReferenceById(STORE_ITEM_ID)).thenReturn(ref);
    }

    private void givenSaved(UserStoreItemEntity entity, UserStoreItem domain) {
        when(userStoreItemJpaRepository.save(any(UserStoreItemEntity.class))).thenReturn(entity);
        when(userStoreItemMapper.toDomain(entity)).thenReturn(domain);
    }

    private void givenAssetKeys(ItemCategory category, List<String> keys) {
        when(userStoreItemJpaRepository.findAssetKeysByUserIdAndCategory(USER_ID, category)).thenReturn(keys);
    }

    private UserStoreItemEntity userItemEntity(Long id) {
        return UserStoreItemEntity.builder().id(id).userId(USER_ID).build();
    }

    private UserStoreItem userItemDomain(Long id) {
        return UserStoreItem.builder().id(id).userId(USER_ID).build();
    }

    private UserStoreItem saveDomain(Instant acquiredAt) {
        return UserStoreItem.builder()
                .userId(USER_ID)
                .storeItem(StoreItem.builder().id(STORE_ITEM_ID).build())
                .equipped(false)
                .acquiredAt(acquiredAt)
                .build();
    }

    private StoreItemEntity storeItemRef() {
        return StoreItemEntity.builder().id(STORE_ITEM_ID).build();
    }

    // --- act ---
    private List<UserStoreItem> findAllByUserId() {
        return userStoreItemRepository.findAllByUserId(USER_ID);
    }

    private boolean isOwned() {
        return userStoreItemRepository.isOwned(USER_ID, STORE_ITEM_ID);
    }

    private UserStoreItem save(UserStoreItem domain) {
        return userStoreItemRepository.save(domain);
    }

    private void updateEquipped(boolean equipped) {
        userStoreItemRepository.updateEquipped(USER_ID, STORE_ITEM_ID, equipped);
    }

    private List<String> findAssetKeys(ItemCategory category) {
        return userStoreItemRepository.findAssetKeysByUserIdAndCategory(USER_ID, category);
    }

    // --- assert ---
    private void thenItemsHaveUserIds(List<UserStoreItem> result, Long... userIds) {
        assertThat(result).extracting(UserStoreItem::getUserId).containsExactly(userIds);
        verify(userStoreItemJpaRepository).findAllByUserId(USER_ID);
    }

    private void thenItemsEmpty(List<UserStoreItem> result) {
        assertThat(result).isEmpty();
        verify(userStoreItemJpaRepository).findAllByUserId(USER_ID);
    }

    private void thenOwned(boolean result) {
        assertThat(result).isTrue();
        verify(userStoreItemJpaRepository).existsByUserIdAndStoreItemId(USER_ID, STORE_ITEM_ID);
    }

    private void thenSavedEntityMatchesAndReturnsMapped(UserStoreItem result, Instant acquiredAt, Long expectedId) {
        ArgumentCaptor<UserStoreItemEntity> captor = ArgumentCaptor.forClass(UserStoreItemEntity.class);
        verify(userStoreItemJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getStoreItem().getId()).isEqualTo(STORE_ITEM_ID);
        assertThat(captor.getValue().getAcquiredAt()).isEqualTo(acquiredAt);
        assertThat(result.getId()).isEqualTo(expectedId);
    }

    private void thenUpdateEquippedDelegated(boolean equipped) {
        verify(userStoreItemJpaRepository).updateEquipped(USER_ID, STORE_ITEM_ID, equipped);
    }

    private void thenAssetKeysAre(List<String> result, ItemCategory category, String... keys) {
        assertThat(result).containsExactly(keys);
        verify(userStoreItemJpaRepository).findAssetKeysByUserIdAndCategory(USER_ID, category);
    }

    private void thenAssetKeysEmpty(List<String> result, ItemCategory category) {
        assertThat(result).isEmpty();
        verify(userStoreItemJpaRepository).findAssetKeysByUserIdAndCategory(USER_ID, category);
    }
}
