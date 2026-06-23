-- Marca de hasta qué expires_at ya se envió el aviso de "plan por vencer".
-- NULL = nunca se avisó (o se renovó el plan). Al avisar se setea = expires_at,
-- de modo que solo se envía un mail por cada expiración.
ALTER TABLE user_plan
    ADD COLUMN IF NOT EXISTS expiry_reminder_sent_for TIMESTAMPTZ;
