package com.huly.backend.domain.repository.user;

import com.huly.backend.domain.model.user.AppUser;
import com.huly.backend.domain.model.enums.SourceAction;

import java.util.List;
import java.util.Optional;
import java.time.Instant;


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
    int debitCoins(Long userId, int amount);
    List<AppUser> findAllNonAdmins();
    Optional<AppUser> findByUnsubscribeToken(String unsubscribeToken);
    void disableReengagementEmails(Long userId);
}
