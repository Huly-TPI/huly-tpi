package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;


import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {

    boolean existsByEmail(String email);

    Optional<AppUserEntity> findByEmail(String email);

    List<AppUserEntity> findByRoleNot(UserRole role);

    @Modifying
    @Query("UPDATE AppUserEntity u SET u.coins = u.coins + :amount WHERE u.id = :userId")
    void addCoins(@Param("userId") Long userId, @Param("amount") int amount);

    @Query("SELECT u.coins FROM AppUserEntity u WHERE u.id = :userId")
    Optional<Integer> findCoinsById(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE AppUserEntity u SET u.coins = u.coins - :amount WHERE u.id = :userId AND u.coins >= :amount")
    int debitCoins(@Param("userId") Long userId, @Param("amount") int amount);
}