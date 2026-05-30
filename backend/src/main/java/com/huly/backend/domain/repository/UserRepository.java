package com.huly.backend.domain.repository;

import com.huly.backend.domain.model.AppUser;

import java.util.Optional;

public interface UserRepository {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    AppUser save(AppUser user);
}
