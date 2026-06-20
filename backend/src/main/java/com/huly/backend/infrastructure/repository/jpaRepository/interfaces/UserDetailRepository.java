package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserDetailRepository
        extends JpaRepository<UserDetailEntity, Long> {
                Optional<UserDetailEntity> findFirstByAppUser_IdOrderByCreatedAtDesc(Long appUserId);

                /**
                 * Usuarios (rol USER, activos) cuyo detail más reciente lleva inactivo más allá
                 * del umbral y a los que todavía no se les envió el recordatorio para esta inactividad.
                 */
                @Query("""
                        SELECT ud FROM UserDetailEntity ud
                        WHERE ud.createdAt = (SELECT MAX(d.createdAt) FROM UserDetailEntity d WHERE d.appUser.id = ud.appUser.id)
                          AND ud.appUser.status = com.huly.backend.domain.model.enums.UserStatus.ACTIVE
                          AND ud.appUser.role = com.huly.backend.domain.model.enums.UserRole.USER
                          AND ud.lastLoginDate IS NOT NULL
                          AND ud.lastLoginDate < :threshold
                          AND (ud.inactivityReminderSentAt IS NULL OR ud.inactivityReminderSentAt <= ud.lastLoginDate)
                        """)
                List<UserDetailEntity> findInactiveNeedingReminder(@Param("threshold") Instant threshold);
}