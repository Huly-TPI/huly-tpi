-- Plan Básico: suscripción mensual con límite de 20 mensajes de chat por día.
INSERT INTO product (name, description, price, coins_amount, type, plan_code, chat_daily_limit)
SELECT 'Plan Basico', 'Acceso premium por 30 días', 5999.00, 0, 'PLAN', 'BASIC', 20
WHERE NOT EXISTS (SELECT 1 FROM product WHERE plan_code = 'BASIC');
