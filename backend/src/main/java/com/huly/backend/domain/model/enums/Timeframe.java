package com.huly.backend.domain.model.enums;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public enum Timeframe {
    TODAY,
    WEEK,
    MONTH,
    TOTAL;

    public Instant getStartInstant(Instant now, ZoneId zone) {
        return switch (this) {
            case TODAY -> now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
            case WEEK -> now.minus(7, ChronoUnit.DAYS);
            case MONTH -> now.minus(30, ChronoUnit.DAYS);
            case TOTAL -> null;
        };
    }

    public static Instant getStartInstantFor(Timeframe timeframe) {
        return timeframe != null ? timeframe.getStartInstant(Instant.now(), ZoneId.systemDefault()) : null;
    }

    public static Timeframe fromString(String value) {
        if (value == null) return TOTAL;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid timeframe: " + value, e);
        }
    }
}
