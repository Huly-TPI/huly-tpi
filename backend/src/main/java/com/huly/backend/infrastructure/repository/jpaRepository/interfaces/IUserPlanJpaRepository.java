package com.huly.backend.infrastructure.repository.jpaRepository.interfaces;

import com.huly.backend.infrastructure.repository.entity.UserPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IUserPlanJpaRepository extends JpaRepository<UserPlanEntity, Long> {
    Optional<UserPlanEntity> findByUserId(Long userId);

    // Planes que vencen dentro de la ventana (now, threshold], cuyo dueño no se dio de baja de los
    // emails, y para cuya expiración actual todavía no se envió aviso.
    @Query("SELECT p FROM UserPlanEntity p JOIN p.user u " +
           "WHERE p.expiresAt > :now AND p.expiresAt <= :threshold " +
           "AND u.reengagementEmailsEnabled = true " +
           "AND (p.expiryReminderSentFor IS NULL OR p.expiryReminderSentFor <> p.expiresAt)")
    List<UserPlanEntity> findPlansNeedingExpiryReminder(@Param("now") Instant now,
                                                        @Param("threshold") Instant threshold);

    @Modifying
    @Query("UPDATE UserPlanEntity p SET p.expiryReminderSentFor = :expiresAt WHERE p.id = :id")
    void markExpiryReminderSent(@Param("id") Long id, @Param("expiresAt") Instant expiresAt);
}
