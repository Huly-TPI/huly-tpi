package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.domain.model.enums.ItemCategory;
import com.huly.backend.infrastructure.repository.entity.StoreItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IStoreItemJpaRepository extends JpaRepository<StoreItemEntity, Long> {
    List<StoreItemEntity> findByCategory(ItemCategory category);
    List<StoreItemEntity> findAllByOrderByPriceCoinsAscPriceAsc();
}
