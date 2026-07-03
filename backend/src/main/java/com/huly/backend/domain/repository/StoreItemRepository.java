package com.huly.backend.domain.repository;
import com.huly.backend.domain.model.shop.StoreItem;
import java.util.List;
import java.util.Optional;

public interface StoreItemRepository {
        List<StoreItem> findAll();
        Optional<StoreItem> findById(Long id);
        StoreItem save(StoreItem item);
        void deleteById(Long id);
    
}
