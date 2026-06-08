package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;

import java.util.Optional;

public interface UserRepository {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    AppUser save(AppUser user);
    void saveLeadDetail(Long userId, String nickname, SourceAction sourceAction);
    void addCoins(Long userId, int amount);
    int getCoins(Long userId);
}
