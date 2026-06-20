package com.huly.backend.domain.model.user;

/** Datos mínimos de un usuario inactivo al que se le debe enviar el email recordatorio. */
public record InactiveUserToRemind(Long userId, String email, String displayName) {
}
