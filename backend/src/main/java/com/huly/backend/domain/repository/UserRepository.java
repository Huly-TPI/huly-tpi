package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

public interface UserRepository {
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findById(Long id);
    boolean existsByEmail(String email);
    AppUser save(AppUser user);
    void saveLeadDetail(Long userId, String nickname, SourceAction sourceAction);
    void addCoins(Long userId, int amount);
    int getCoins(Long userId);
    void updateLastLogin(Long userId);
    List<AppUser> findUsersInactiveSince(Instant since);

}
