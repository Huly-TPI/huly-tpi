WITH ranked_growing_plants AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY id_app_user
               ORDER BY plant_number DESC, started_at DESC, id DESC
           ) AS row_num
    FROM user_plant
    WHERE status = 'GROWING'
)
UPDATE user_plant user_plant_to_fix
SET status = 'COMPLETED',
    completed_at = COALESCE(user_plant_to_fix.completed_at, NOW())
FROM ranked_growing_plants ranked
WHERE user_plant_to_fix.id = ranked.id
  AND ranked.row_num > 1;

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_plant_one_growing_per_user
    ON user_plant (id_app_user)
    WHERE status = 'GROWING';
