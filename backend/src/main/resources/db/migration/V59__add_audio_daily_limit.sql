-- Límite diario de mensajes de audio por plan. NULL = ilimitado.
-- Solo aplica a productos de tipo PLAN.
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS audio_daily_limit INTEGER;

-- Plan Básico: 3 audios por día
UPDATE product
SET audio_daily_limit = 3
WHERE plan_code = 'BASIC';

-- Plan Premium: sin límite (NULL)
