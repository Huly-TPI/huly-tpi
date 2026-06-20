-- Marca de envío del email recordatorio de inactividad, para no reenviarlo
-- hasta que el usuario vuelva a loguearse (last_login_date avanza por encima de este valor).
ALTER TABLE user_detail
    ADD COLUMN IF NOT EXISTS inactivity_reminder_sent_at TIMESTAMPTZ;
