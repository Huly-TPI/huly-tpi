package com.huly.backend.infrastructure.config.useCase;

import java.time.ZoneId;

/**
 * Parámetros únicos de la política de re-enganche por inactividad.
 * Compartidos por la recompensa de retorno (login) y el email recordatorio (scheduler),
 * para que el umbral y el monto no se desincronicen.
 */
public final class RewardPolicy {

    /** Zona horaria de negocio para definir "el día". */
    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    /** Días de inactividad a partir de los cuales aplica la recompensa / el email. */
    public static final int INACTIVITY_THRESHOLD_DAYS = 5;

    /** Monedas otorgadas por volver tras la inactividad. */
    public static final int COMEBACK_REWARD_COINS = 30;

    private RewardPolicy() {
    }
}
