package com.huly.backend.domain.model.dailyReward;

import java.time.LocalDate;

/**
 * Estado de la racha de recompensas diarias de un usuario.
 *
 * @param streak        último día del ciclo reclamado (0 = nunca reclamó).
 * @param lastClaimDate fecha del último reclamo (null si nunca reclamó).
 */
public record DailyClaimState(int streak, LocalDate lastClaimDate) {
}
