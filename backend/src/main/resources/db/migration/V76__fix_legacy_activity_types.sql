-- Clean up any remaining legacy, Spanish, accented, or plural activity type values to match current English enums
-- 1. Update activity table
UPDATE activity
SET type = CASE 
    WHEN type IN ('ARENA_ZEN', 'ZEN_GARDEN') THEN 'ZEN_GARDEN'
    WHEN type IN ('RESPIRACION', 'RESPIRACIÓN', 'BREATHING') THEN 'BREATHING'
    WHEN type IN ('DIARIO', 'DIARY') THEN 'DIARY'
    WHEN type IN ('NUBE', 'NUBES', 'LANTERN') THEN 'LANTERN'
    WHEN type IN ('BURBUJA', 'BURBUJAS', 'BUBBLE') THEN 'BUBBLE'
    WHEN type IN ('RETO', 'RETOS', 'CHALLENGE') THEN 'CHALLENGE'
    WHEN type IN ('MANDALA', 'MANDALAS') THEN 'MANDALA'
    WHEN type IN ('STONES', 'PIEDRAS') THEN 'STONES'
    WHEN type IN ('PENDING', 'PENDIENTES') THEN 'PENDING'
    ELSE type
END
WHERE type IN ('ARENA_ZEN', 'RESPIRACION', 'RESPIRACIÓN', 'DIARIO', 'NUBE', 'NUBES', 'BURBUJA', 'BURBUJAS', 'RETO', 'RETOS', 'MANDALAS', 'PIEDRAS', 'PENDIENTES');

-- 2. Update activity_sessions table
UPDATE activity_sessions
SET activity_type = CASE 
    WHEN activity_type IN ('ARENA_ZEN', 'ZEN_GARDEN') THEN 'ZEN_GARDEN'
    WHEN activity_type IN ('RESPIRACION', 'RESPIRACIÓN', 'BREATHING') THEN 'BREATHING'
    WHEN activity_type IN ('DIARIO', 'DIARY') THEN 'DIARY'
    WHEN activity_type IN ('NUBE', 'NUBES', 'LANTERN') THEN 'LANTERN'
    WHEN activity_type IN ('BURBUJA', 'BURBUJAS', 'BUBBLE') THEN 'BUBBLE'
    WHEN activity_type IN ('RETO', 'RETOS', 'CHALLENGE') THEN 'CHALLENGE'
    WHEN activity_type IN ('MANDALA', 'MANDALAS') THEN 'MANDALA'
    WHEN activity_type IN ('STONES', 'PIEDRAS') THEN 'STONES'
    WHEN activity_type IN ('PENDING', 'PENDIENTES') THEN 'PENDING'
    ELSE activity_type
END
WHERE activity_type IN ('ARENA_ZEN', 'RESPIRACION', 'RESPIRACIÓN', 'DIARIO', 'NUBE', 'NUBES', 'BURBUJA', 'BURBUJAS', 'RETO', 'RETOS', 'MANDALAS', 'PIEDRAS', 'PENDIENTES');

-- 3. Update chat_message table (suggested_action_type)
UPDATE chat_message
SET suggested_action_type = CASE 
    WHEN suggested_action_type IN ('ARENA_ZEN', 'ZEN_GARDEN') THEN 'ZEN_GARDEN'
    WHEN suggested_action_type IN ('RESPIRACION', 'RESPIRACIÓN', 'BREATHING') THEN 'BREATHING'
    WHEN suggested_action_type IN ('DIARIO', 'DIARY') THEN 'DIARY'
    WHEN suggested_action_type IN ('NUBE', 'NUBES', 'LANTERN') THEN 'LANTERN'
    WHEN suggested_action_type IN ('BURBUJA', 'BURBUJAS', 'BUBBLE') THEN 'BUBBLE'
    WHEN suggested_action_type IN ('RETO', 'RETOS', 'CHALLENGE') THEN 'CHALLENGE'
    WHEN suggested_action_type IN ('MANDALA', 'MANDALAS') THEN 'MANDALA'
    WHEN suggested_action_type IN ('STONES', 'PIEDRAS') THEN 'STONES'
    WHEN suggested_action_type IN ('PENDING', 'PENDIENTES') THEN 'PENDING'
    ELSE suggested_action_type
END
WHERE suggested_action_type IN ('ARENA_ZEN', 'RESPIRACION', 'RESPIRACIÓN', 'DIARIO', 'NUBE', 'NUBES', 'BURBUJA', 'BURBUJAS', 'RETO', 'RETOS', 'MANDALAS', 'PIEDRAS', 'PENDIENTES');
