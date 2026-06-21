package com.huly.backend.domain.model.comebackReward;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Reglas de la recompensa por volver a la app tras un período de inactividad.
 *
 * La inactividad se mide como los días transcurridos entre la última fecha de actividad
 * registrada del usuario ({@code lastSeen}) y hoy. Si alcanza {@link #INACTIVE_DAYS_THRESHOLD}
 * el usuario califica para la recompensa de {@link #COMEBACK_COINS} monedas.
 */
public final class ComebackRewardPolicy {

    private ComebackRewardPolicy() {
    }

    /** Días de inactividad requeridos para otorgar la recompensa de regreso. */
    public static final int INACTIVE_DAYS_THRESHOLD = 10;

    /** Monedas otorgadas al volver tras el período de inactividad. */
    public static final int COMEBACK_COINS = 50;

    /** Días transcurridos desde la última actividad (0 si nunca se registró). */
    public static long daysInactive(LocalDate lastSeen, LocalDate today) {
        return lastSeen == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(lastSeen, today));
    }

    /** True si la brecha de inactividad alcanza el umbral. */
    public static boolean qualifies(LocalDate lastSeen, LocalDate today) {
        return lastSeen != null && daysInactive(lastSeen, today) >= INACTIVE_DAYS_THRESHOLD;
    }

    /**
     * True si hay que registrar la actividad de hoy (avanzar la fecha): solo cuando NO hay un
     * comeback pendiente, para no borrar la brecha antes de que el usuario pueda reclamarlo.
     */
    public static boolean shouldRegisterActivity(LocalDate lastSeen, LocalDate today) {
        return !qualifies(lastSeen, today);
    }
}
