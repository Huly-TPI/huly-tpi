-- last_login_date pasa a representar la "fecha de última actividad" (base de la recompensa de
-- regreso). Se convierte de TIMESTAMPTZ a DATE. La columna nunca se seteó, así que la conversión
-- no pierde datos.
ALTER TABLE IF EXISTS user_detail
ALTER COLUMN last_login_date TYPE DATE USING last_login_date::date;
