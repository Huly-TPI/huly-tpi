package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.domain.model.user.UserStoreItem;

import java.util.List;

public interface UserStoreItemRepository {
    List<UserStoreItem> findAllByUserId(Long userId);
    boolean isOwned(Long userId, Long storeItemId);
    UserStoreItem save(UserStoreItem userStoreItem);
    void updateEquipped(Long userId, Long storeItemId, boolean equipped);
    List<String> findAssetKeysByUserIdAndCategory(Long userId, ItemCategory category);
}
