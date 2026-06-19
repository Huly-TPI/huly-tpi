package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.UserStoreItem;
import java.util.List;
public interface UserStoreItemRepository {
    List<UserStoreItem> findAllByUserId(Long userId);
    boolean isOwned(Long userId, Long storeItemId);
    UserStoreItem save(UserStoreItem userStoreItem);
    void updateEquipped(Long userId, Long storeItemId, boolean equipped);
}
