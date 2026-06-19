package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;
import com.huly.backend.infrastructure.repository.entity.UserStoreItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IUserStoreItemJpaRepository extends JpaRepository<UserStoreItemEntity, Long> {

    List<UserStoreItemEntity> findAllByUserId(Long userId);

    boolean existsByUserIdAndStoreItemId(Long userId, Long storeItemId);

    @Modifying
    @Query("UPDATE UserStoreItemEntity u SET u.equipped = :equipped WHERE u.userId = :userId AND u.storeItem.id = :storeItemId")

    void updateEquipped(@Param("userId") Long userId, @Param("storeItemId") Long storeItemId, @Param("equipped") boolean equipped);
}
