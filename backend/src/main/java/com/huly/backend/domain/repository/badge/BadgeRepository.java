package com.huly.backend.domain.repository.badge;
import com.huly.backend.domain.model.Badge;
import java.util.List;
import java.util.Optional;

public interface BadgeRepository {
    List<Badge> findAll();
    Optional<Badge> findByCode(String code);
}
